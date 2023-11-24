package livecast.agent.controller.support;

import livecast.agent.exception.BaseException;
import livecast.agent.exception.ErrorCode;
import livecast.agent.exception.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(HttpServletRequest httpRequest, BaseException ex) {
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        final ErrorResponse response = getErrorResponse(ex, ex.getErrorCode(), httpRequest, status, true);

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(HttpServletRequest httpRequest, AuthenticationException ex) {
        final HttpStatus status = HttpStatus.UNAUTHORIZED;
        final ErrorResponse response = getErrorResponse(ex, ErrorCode.AuthenticationFail, httpRequest, status, false);

        return new ResponseEntity<>(response, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        HttpServletRequest httpServletRequest = null;
        if(request instanceof ServletWebRequest) {
            httpServletRequest = ((ServletWebRequest) request).getNativeRequest(HttpServletRequest.class);
        }
        final ErrorResponse response = getErrorResponse(ex, ErrorCode.Unknown, httpServletRequest, status, true);

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(HttpServletRequest httpRequest, Exception ex) {
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        final ErrorResponse response = getErrorResponse(ex, ErrorCode.Unknown, httpRequest, status, true);

        return new ResponseEntity<>(response, status);
    }

    private ErrorResponse getErrorResponse(Exception ex, ErrorCode errorCode, HttpServletRequest httpServletRequest, HttpStatus status, boolean logException) {
        String requestPath = "";
        if(httpServletRequest != null) {
            requestPath = httpServletRequest.getRequestURI();

            if (httpServletRequest.getQueryString() != null) {
                requestPath += "?" + httpServletRequest.getQueryString();
            }
        }
        String remoteAddress = "";
        if(httpServletRequest != null) {
            remoteAddress = httpServletRequest.getRemoteAddr();
        }

        final ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .errorCode(errorCode)
                .message(ex.getLocalizedMessage())
                .path(requestPath)
                .build();

        if(logException) {
            logger.warn("Exception", ex);
        }
        logger.info("HTTP Response [{} {}] to {} : {}", status.value(), status.name(), remoteAddress, response);

        return response;
    }
}
