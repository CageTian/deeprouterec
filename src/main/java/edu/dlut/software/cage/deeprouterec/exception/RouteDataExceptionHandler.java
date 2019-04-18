package edu.dlut.software.cage.deeprouterec.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

@ControllerAdvice
@Slf4j
public class RouteDataExceptionHandler extends ExceptionHandlerExceptionResolver {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleInvalidRequest(MissingServletRequestParameterException ex) {
        log.error(HttpStatus.BAD_REQUEST.getReasonPhrase(), ex);

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExceptionInInitializerError.class)
    public ResponseEntity<String> handleExceptionInInitializerError(ExceptionInInitializerError ex) {
        log.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), ex);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), ex);

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
