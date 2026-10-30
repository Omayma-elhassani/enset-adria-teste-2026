package org.sid.chatbotservice.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sid.chatbotservice.dto.ChatRequest;
import org.sid.chatbotservice.dto.ChatResponse;
import org.sid.chatbotservice.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "Assistant bancaire agentique (Spring AI + MCP)")
public class ChatRestController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Envoyer un message à l'agent (réutiliser conversationId pour garder le contexte)")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    @GetMapping
    @Operation(summary = "Question rapide sans contexte de conversation")
    public ChatResponse ask(@RequestParam String message) {
        return chatService.chat(new ChatRequest(null, message));
    }

    @GetMapping("/tools")
    @Operation(summary = "Lister les tools MCP disponibles pour l'agent")
    public List<String> tools() {
        return chatService.availableTools();
    }
}
