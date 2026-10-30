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
| `account-service` | Gestion des comptes bancaires | 8085 |
| `transaction-service` | Enregistrement et consultation des transactions | 8082 |
| `chatbot-service` | Agent IA (Spring AI, client MCP) | 8083 |
| `notification-service` | Consumer Kafka des événements de transaction | 8084 |
| `fraud-detection/` | Notebook Python de détection de fraude | - |
| `frontend/` | Client Angular | 4200 |

Chaque micro-service métier respecte la séparation des couches :
`domain/`, `repository/`, `service/`, `web/rest/`, `web/graphql/`, `mcp/`.
Les contrôleurs REST, GraphQL et MCP appellent tous la même couche `service`.

## Partie C : endpoints
| Service | REST / Swagger | GraphQL | MCP |
|---|---|---|---|
| account-service | http://localhost:8085/swagger-ui.html | http://localhost:8085/graphiql | POST http://localhost:8085/mcp |
| transaction-service | http://localhost:8082/swagger-ui.html | http://localhost:8082/graphiql | POST http://localhost:8082/mcp |

Comptes de test : `11111111-...` (MAD, 15000), `22222222-...` (MAD, 3000), `33333333-...` (EUR, 2500),
`44444444-...` (MAD, BLOCKED). Scénarios de test : [`http/partie-c.http`](http/partie-c.http).

## Partie D : services techniques
Ordre de démarrage : `discovery-service` → `config-service` → `account-service` → `transaction-service` → `gateway-service`.

- Eureka : http://localhost:8761
- Config Server : http://localhost:9999/transaction-service/default (`bank.transaction.max-amount=1000`,
  fichier `config-service/src/main/resources/config-repo/transaction-service.properties`)
- Gateway : http://localhost:8888/api/accounts, http://localhost:8888/api/transactions
  (et routes dynamiques `http://localhost:8888/<service>/...`)

Scénarios de test : [`http/partie-d.http`](http/partie-d.http).

## Partie E : chatbot agentique
`chatbot-service` (port 8083) : agent Spring AI connecté à **Google Gemini**. Il utilise comme tools les serveurs MCP
d'`account-service` et de `transaction-service` (client MCP Streamable HTTP), avec une mémoire par conversation.

- Clé Gemini gratuite : https://aistudio.google.com/apikey, à mettre dans la variable d'environnement `GEMINI_API_KEY`
  (jamais dans le code).
- `POST /api/chat` `{ "conversationId": "demo-1", "message": "Quel est le solde de CUST-001 ?" }`
- `GET /api/chat/tools` : tools MCP découverts. Swagger : http://localhost:8083/swagger-ui.html
- Scénarios : [`http/partie-e.http`](http/partie-e.http)

## Avancement
- [x] Partie A : préparation (projet Maven multi-modules)
- [x] Partie B : conception et architecture
- [x] Partie C : micro-services fonctionnels (REST, GraphQL, MCP, OpenFeign, Resilience4J)
- [x] Partie D : micro-services techniques (Gateway, Eureka, Config)
- [x] Partie E : chatbot agentique
- [ ] Partie F : communication asynchrone avec Kafka
- [ ] Partie G : client web Angular
- [ ] Partie H : IA avec Python (détection de fraude)
- [ ] Partie I : DevOps
