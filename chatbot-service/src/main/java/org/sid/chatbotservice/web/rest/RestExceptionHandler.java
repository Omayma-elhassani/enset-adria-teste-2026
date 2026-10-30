package org.sid.chatbotservice.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Renvoie la cause réelle de l'erreur (clé Gemini, quota, modèle inconnu...) au lieu d'un 500 sans détail. */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handle(Exception e) {
        log.error("Erreur du chatbot", e);
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                root.getClass().getSimpleName() + " : " + root.getMessage());
        pd.setTitle("Erreur de l'agent IA");
        pd.setProperty("error", e.getMessage());
        return pd;
    }
}
