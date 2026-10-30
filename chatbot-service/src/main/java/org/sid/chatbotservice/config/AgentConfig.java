package org.sid.chatbotservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de l'agent :
 * - LLM Gemini (auto-configuré par spring-ai-starter-model-google-genai) ;
 * - tools MCP récupérés à chaque requête sur account-service et transaction-service (voir ChatService),
 *   pour que le chatbot démarre même si un service métier est arrêté ;
 * - mémoire de conversation (20 derniers messages par conversation).
 */
@Configuration
public class AgentConfig {

    static final String SYSTEM_PROMPT = """
            Tu es l'assistant bancaire de la plateforme ADRIA Bank.
            Tu aides les utilisateurs à consulter leurs comptes, leurs transactions et à effectuer des opérations.

            Règles :
            - Utilise TOUJOURS les tools disponibles pour obtenir des données réelles ; n'invente jamais un solde,
              un identifiant ou une transaction.
            - Pour retrouver un compte à partir d'une référence client (ex : CUST-001), utilise le tool qui liste
              les comptes d'un client, puis utilise l'identifiant UUID obtenu.
            - Avant une opération qui modifie des données (transaction, virement, blocage de compte), vérifie que la
              demande est claire (compte, montant, devise). S'il manque une information, pose la question.
            - Si un tool renvoie une erreur (plafond dépassé, compte bloqué, devise différente, solde insuffisant...),
              explique simplement la raison à l'utilisateur.
            - Réponds en français, de façon concise, et présente les montants avec leur devise.
            """;

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
