export type AccountStatus = 'ACTIVE' | 'BLOCKED';
export type TransactionType = 'DEBIT' | 'CREDIT';

export interface Account {
  id: string;
  customerReference: string;
  status: AccountStatus;
  currency: string;
  balance: number;
}

export interface AccountRequest {
  customerReference: string;
  currency: string;
  initialBalance: number;
}

export interface Transaction {
  id: string;
  accountId: string;
  amount: number;
  currency: string;
  type: TransactionType;
  merchantCategory: string | null;
  country: string | null;
  createdAt: string;
}

export interface TransactionRequest {
  accountId: string;
  amount: number;
  currency: string;
  type: TransactionType;
  merchantCategory?: string;
  country?: string;
}

export interface TransferRequest {
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  currency: string;
}

export interface ChatResponse {
  conversationId: string;
  answer: string;
}

export interface TransactionCreatedEvent {
  eventId: string;
  eventType: string;
  transactionId: string;
  accountId: string;
  amount: number;
  currency: string;
  occurredAt: string;
}

export interface ReceivedEvent {
  key: string;
  partition: number;
  offset: number;
  receivedAt: string;
  event: TransactionCreatedEvent;
}
