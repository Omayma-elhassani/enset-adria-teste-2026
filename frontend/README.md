# Partie G : client web Angular

Client Angular 22 (composants standalone, signals) de la plateforme bancaire. Toutes les requêtes passent par la
Gateway `http://localhost:8888` (voir `src/environments/environment.ts`).

| Page | Route | Service appelé |
|---|---|---|
| Comptes (recherche, filtre, pagination, création) | `/accounts` | account-service |
| Détail d'un compte (solde, blocage, historique, nouvelle transaction) | `/accounts/:id` | account-service + transaction-service |
| Virement | `/transfer` | transaction-service |
| Assistant IA (chat) | `/chat` | chatbot-service |
| Événements Kafka | `/events` | notification-service |

## Lancement
Prérequis : Node.js 20.19+ ou 22.12+ (https://nodejs.org), et les services + la Gateway démarrés.

```bash
cd frontend
npm install
npm start          # http://localhost:4200
```
