package org.sid.chatbotservice.service;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.chatbotservice.dto.ChatRequest;
import org.sid.chatbotservice.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    /** Un client MCP par serveur (account-service, transaction-service). Connexion paresseuse. */
    private final List<McpSyncClient> mcpClients;

    public ChatResponse chat(ChatRequest request) {
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.conversationId();

        String answer = chatClient.prompt()
                .user(request.message())
                .toolCallbacks(loadTools())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        return new ChatResponse(conversationId, answer);
    }

    /** Liste des tools MCP disponibles pour l'agent (utile pour vérifier la connexion aux serveurs MCP). */
    public List<String> availableTools() {
        return loadTools().stream()
                .map(t -> t.getToolDefinition().name() + " : " + t.getToolDefinition().description())
                .toList();
    }

    /**
     * Récupère les tools de chaque serveur MCP. Si un micro-service est arrêté, ses tools sont simplement
     * ignorés (tolérance aux pannes) au lieu de faire échouer tout le chatbot.
     */
    private List<ToolCallback> loadTools() {
        List<ToolCallback> tools = new ArrayList<>();
        for (McpSyncClient client : mcpClients) {
            try {
                tools.addAll(Arrays.asList(
                        SyncMcpToolCallbackProvider.builder().mcpClients(client).build().getToolCallbacks()));
            } catch (Exception e) {
                log.warn("Serveur MCP indisponible, tools ignorés : {}", e.getMessage());
            }
        }
        return tools;
    }
}
