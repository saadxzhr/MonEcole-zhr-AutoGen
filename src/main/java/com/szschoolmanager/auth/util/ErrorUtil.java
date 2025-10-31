package com.szschoolmanager.auth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * Utilitaire centralisé pour écrire des réponses d'erreur JSON sécurisées.
 */
public final class ErrorUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ErrorUtil() {}

    public static void writeJsonError(HttpServletResponse response, HttpStatus status, String message) {
        try {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            var body = new ErrorResponse("error", message, status.value());
            response.getWriter().write(MAPPER.writeValueAsString(body));
            response.getWriter().flush();
        } catch (IOException e) {
            // En dernier recours, renvoyer le statut sans body
            response.setStatus(status.value());
        }
    }

    // petit DTO immuable pour la structure d'erreur
    public static final class ErrorResponse {
        public final String status;
        public final String message;
        public final int code;
        public ErrorResponse(String status, String message, int code) {
            this.status = status;
            this.message = message;
            this.code = code;
        }
    }
}
