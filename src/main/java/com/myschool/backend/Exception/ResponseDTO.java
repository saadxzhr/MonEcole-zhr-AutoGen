package com.myschool.backend.exception;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Structure standard des réponses de l'API")
public class ResponseDTO<T> {
    private String status;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();
    

    public static <T> ResponseDTO<T> success(String message, T data) {
        return new ResponseDTO<>("SUCCESS", message, data, LocalDateTime.now());
    }

    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>("ERROR", message, null, LocalDateTime.now());
    }

    


}
