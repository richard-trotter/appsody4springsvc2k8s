package demo.inventory.api.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import demo.inventory.api.model.ApiError;

/*
 * Handle exceptions for all REST endpoints within the application.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
  private static Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  public ApiExceptionHandler() {
    super();
  }  
  
  
  /*
   * Handle HttpClientErrorException, assuming this is thrown to indicate some kind of 
   * "bad request". 
   */
  @ExceptionHandler(value = HttpClientErrorException.class)
  public ResponseEntity<ApiError> handleClientError(HttpClientErrorException ex) {
    return ResponseEntity
        .status(ex.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ApiError(ex.getStatusCode().value(), ex.getStatusText(), "no detail available"));
  }
  
  
  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleClientError(MethodArgumentNotValidException ex) {
    
    log.warn(ex.getMessage());
    
    FieldError f = ex.getBindingResult().getFieldError();
    String msg = String.format("\"%1$s\" %2$s",f.getField(), f.getDefaultMessage());
    
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ApiError(HttpStatus.BAD_REQUEST.value(), msg, "no detail available"));    
  }
  
  
  @ExceptionHandler(value = ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleClientError(ConstraintViolationException ex) {
    
    log.warn(ex.getMessage());
    
    Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
    String msg = violations.stream()
        .map(v -> String.format("\"%1$s\" %2$s", v.getPropertyPath(), v.getMessage()))
        .collect(Collectors.joining(", "));
    
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ApiError(HttpStatus.BAD_REQUEST.value(), msg, "no detail available"));    
  }
  
  /*
   * Handle any other Exception. Assume this is not intentional, and
   * log an exception stack trace.
   */
  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<Map<String, String>> handleException(Exception ex) {

    log.warn("handling exception", ex);

    Map<String, String> errorResponse = new HashMap<String, String>();
    errorResponse.put("errorMessage", ex.toString());

    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse);
  }
  
}
