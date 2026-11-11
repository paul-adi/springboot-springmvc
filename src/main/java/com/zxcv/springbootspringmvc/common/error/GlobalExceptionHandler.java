package com.zxcv.springbootspringmvc.common.error;

import com.zxcv.springbootspringmvc.common.logging.ExceptionLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_CODE = "errorCode";
    private static final String TRACE_ID = "traceId";
    private static final Set<String> MANDATORY_CONSTRAINTS = Set.of("NotNull", "NotBlank", "NotEmpty");

    public record FieldViolation(String field, String reason) {
        static FieldViolation required(String field) {
            return new FieldViolation(field, "REQUIRED");
        }
    }

    public static ProblemDetail buildProblemDetail(ErrorCode code) {
        var problemDetail = ProblemDetail.forStatusAndDetail(code.status(), code.message());
        problemDetail.setTitle(code.status().getReasonPhrase());
        problemDetail.setProperty(ERROR_CODE, code.code());
        problemDetail.setProperty(TRACE_ID, currentTraceId());
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        return handleValidationProblem(ex, List.of(FieldViolation.required(ex.getParameterName())),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        if (ex instanceof MissingRequestHeaderException missingHeader) {
            return handleValidationProblem(ex, List.of(FieldViolation.required(missingHeader.getHeaderName())),
                    headers, status, request);
        }
        return super.handleServletRequestBindingException(ex, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> new FieldViolation(
                        fieldError.getField(),
                        MANDATORY_CONSTRAINTS.contains(fieldError.getCode()) ? "REQUIRED" : "INVALID_FORMAT"))
                .toList();
        return handleValidationProblem(ex, violations, headers, status, request);
    }

    private ResponseEntity<Object> handleValidationProblem(
            Exception ex, List<FieldViolation> violations, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        var body = buildProblemDetail(CommonErrorCode.VALIDATION_FAILED);
        body.setProperty("violations", violations);
        log.warn("Validation failed. Violations : {}", violations);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request
    ) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);

        if (response != null && response.getBody() instanceof ProblemDetail problemDetail) {
            Map<String, Object> properties = problemDetail.getProperties();
            if (properties == null || !properties.containsKey(ERROR_CODE)) {
                problemDetail.setProperty(ERROR_CODE, "HTTP-" + statusCode.value());
            }
            problemDetail.setProperty(TRACE_ID, currentTraceId());
        }

        log.atLevel(levelFor(statusCode))
                .log("Request rejected. Exception summary : {}", ExceptionLogUtils.summarize(ex));
        return response;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleException(Exception ex) {
        log.error("Unexpected error. Exception summary : {}", ExceptionLogUtils.summarize(ex), ex);
        return buildProblemDetail(CommonErrorCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    ProblemDetail handleBusinessException(BusinessException ex) {
        log.atLevel(levelFor(ex.getErrorCode().status()))
                .log("Business error. Exception summary : {}", ExceptionLogUtils.summarize(ex));
        return buildProblemDetail(ex.getErrorCode());
    }

    private static String currentTraceId() {
        return MDC.get(TRACE_ID);
    }

    private static Level levelFor(HttpStatusCode status) {
        return status.is5xxServerError() ? Level.ERROR : Level.WARN;
    }

}
