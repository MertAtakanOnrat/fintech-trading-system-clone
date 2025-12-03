package com.fintech.wallet.exception;

import com.fintech.common.exception.InsufficientBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bizim özel hatamızı yakala
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientBalance(InsufficientBalanceException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Yetersiz Bakiye");
        errorResponse.put("message", ex.getMessage());

        // 400 Bad Request dönüyoruz
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Diğer tüm beklenmedik hataları yakala
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Sistem Hatası");
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
