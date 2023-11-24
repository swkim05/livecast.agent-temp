package livecast.agent.controller.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class ControllerLogger {
    private static final Logger logger = LoggerFactory.getLogger(ControllerLogger.class);

    @Around("execution(* livecast.agent.controller.*Controller.*(..)) && args(httpRequest, ..)")
    public Object logServiceController(ProceedingJoinPoint joinPoint, HttpServletRequest httpRequest) throws Throwable {
        return logController(joinPoint, httpRequest);
    }

    private Object logController(ProceedingJoinPoint joinPoint, HttpServletRequest httpRequest) throws Throwable {
        Object requestBody = getAnnotatedParameterValue(joinPoint, RequestBody.class);

        String requestPath = httpRequest.getRequestURI();
        if(httpRequest.getQueryString() != null) {
            requestPath += "?" + httpRequest.getQueryString();
        }

        long start = System.currentTimeMillis();
        logHttpRequest(httpRequest, requestPath, requestBody);

        Object returnObj = joinPoint.proceed();

        logHttpResponse(httpRequest, returnObj);
        long end = System.currentTimeMillis();

        logHttp(httpRequest, requestPath, returnObj, start, end);
        return returnObj;
    }

    private <T extends Annotation> Object getAnnotatedParameterValue(ProceedingJoinPoint joinPoint, Class<T> annotationType) {
        Object value = null;

        int index = getAnnotatedParameterIndex(joinPoint, annotationType);
        if(index >= 0) {
            Object[] args = joinPoint.getArgs();
            if(index < args.length) {
                value = args[index];
            }
        }

        return value;
    }

    private <T extends Annotation> int getAnnotatedParameterIndex(ProceedingJoinPoint joinPoint, Class<T> annotationType) {
        int index = -1;

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        for(int i=0; i<parameters.length; i++) {
            if(parameters[i].isAnnotationPresent(annotationType)) {
                index = i;
                break;
            }
        }

        return index;
    }

    private void logHttpRequest(HttpServletRequest httpRequest, String requestPath, Object requestBody) {
        if(requestBody != null) {
            logger.debug("HTTP Request [{}] {} from {} : {}", httpRequest.getMethod(), requestPath, httpRequest.getRemoteAddr(), requestBody.toString());
        } else {
            logger.debug("HTTP Request [{}] {} from {}", httpRequest.getMethod(), requestPath, httpRequest.getRemoteAddr());
        }
    }

    @SuppressWarnings("unchecked")
    private void logHttpResponse(HttpServletRequest httpRequest, Object response) {
        HttpStatus responseStatus = ((response != null) && (response instanceof ResponseEntity))
                ? ((ResponseEntity)response).getStatusCode()
                : HttpStatus.OK;
        Object responseBody = ((response != null) && (response instanceof ResponseEntity) && (((ResponseEntity)response).getBody() != null))
                ? ((ResponseEntity)response).getBody()
                : response;

        if(responseBody != null) {
            logger.debug("HTTP Response [{} {}] to {} : {}", responseStatus.value(), responseStatus.name(), httpRequest.getRemoteAddr(), responseBody.toString());
        } else {
            logger.debug("HTTP Response [{} {}] to {}", responseStatus.value(), responseStatus.name(), httpRequest.getRemoteAddr());
        }
    }

    private void logHttp(HttpServletRequest httpRequest, String requestPath, Object response, long start, long end) {
        HttpStatus responseStatus = ((response != null) && (response instanceof ResponseEntity))
                ? ((ResponseEntity)response).getStatusCode()
                : HttpStatus.OK;
        double gapSeconds = ((double)(end - start) / (double)1000);
        String gapString = String.format("%.3f", gapSeconds);

        logger.info("{} [{}] {} from {} -> [{}] {} : {} seconds",
                httpRequest.getProtocol(),
                httpRequest.getMethod(),
                requestPath,
                httpRequest.getRemoteAddr(),
                responseStatus.value(),
                responseStatus.name(),
                gapString
        );
    }
}
