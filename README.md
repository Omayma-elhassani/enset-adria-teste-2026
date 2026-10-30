# Test ADRIA ENSET 2027 : plateforme bancaire micro-services

Plateforme bancaire qui permet de consulter des comptes, d'enregistrer des transactions et de publier des
événements métier. Un assistant conversationnel (Spring AI + MCP) interroge les données.

## Stack technique
- **Backend** : Java 17, Spring Boot 4.1, Spring Cloud 2025.1 (Gateway, Eureka, Config, OpenFeign, Resilience4J)
- **Exposition** : REST, GraphQL, MCP
- **IA** : Spring AI (agent + tools MCP), Python / scikit-learn (détection de fraude)
- **Messaging** : Kafka (`bank.transactions.created`)
- **Sécurité** : Keycloak
- **Frontend** : Angular
- **DevOps** : Docker, Docker Compose, GitHub Actions

## Structure du projet (Maven multi-modules)
| Module | Rôle | Port |
|---|---|---|
| `config-service` | Configuration centralisée (Spring Cloud Config) | 9999 |
| `discovery-service` | Annuaire Eureka | 8761 |
| `gateway-service` | Point d'entrée unique (Spring Cloud Gateway) | 8888 |
| `account-service` | Gestion des comptes bancaires | 8081 |
| `transaction-service` | Enregistrement et consultation des transactions | 8082 |
| `chatbot-service` | Agent IA (Spring AI, client MCP) | 8083 |
| `notification-service` | Consumer Kafka des événements de transaction | 8084 |
| `fraud-detection/` | Notebook Python de détection de fraude | - |
| `frontend/` | Client Angular | 4200 |

Chaque micro-service métier respecte la séparation des couches :
`domain/`, `repository/`, `service/`, `web/rest/`, `web/graphql/`, `mcp/`.
Les contrôleurs REST, GraphQL et MCP appellent tous la même couche `service`.

## Avancement
- [x] Partie A : préparation (projet Maven multi-modules)
- [ ] Partie B : conception et architecture
- [ ] Partie C : micro-services fonctionnels (REST, GraphQL, MCP, OpenFeign, Resilience4J)
- [ ] Partie D : micro-services techniques (Gateway, Eureka, Config)
- [ ] Partie E : chatbot agentique
- [ ] Partie F : communication asynchrone avec Kafka
- [ ] Partie G : client web Angular
- [ ] Partie H : IA avec Python (détection de fraude)
- [ ] Partie I : DevOps
