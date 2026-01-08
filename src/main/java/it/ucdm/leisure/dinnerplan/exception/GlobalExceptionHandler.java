package it.ucdm.leisure.dinnerplan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public org.springframework.web.servlet.view.RedirectView handleLogicalExceptions(RuntimeException ex,
            jakarta.servlet.http.HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return new org.springframework.web.servlet.view.RedirectView(referer != null ? referer : "/");
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.web.servlet.view.RedirectView handleGenericException(Exception ex,
            jakarta.servlet.http.HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        // Log the error (optional, using sysout for now or rely on aspect)
        ex.printStackTrace();
        redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        String referer = request.getHeader("Referer");
        return new org.springframework.web.servlet.view.RedirectView(referer != null ? referer : "/");
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Object> handleSecurityException(SecurityException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // You can add more exception handlers here as needed
}
