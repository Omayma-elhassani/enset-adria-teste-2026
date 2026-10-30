package org.sid.chatbotservice.dto;

import jakarta.validation.constraints.NotBlank;

/** conversationId est optionnel : s'il est absent, une nouvelle conversation est créée. */
public record ChatRequest(String conversationId, @NotBlank String message) {
}
