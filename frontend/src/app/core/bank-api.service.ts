import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import {
  Account, AccountRequest, AccountStatus, ChatResponse, ReceivedEvent,
  Transaction, TransactionRequest, TransferRequest,
} from './models';

/** Client des micro-services, via la Gateway (REST). */
@Injectable({ providedIn: 'root' })
export class BankApiService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  // ----- account-service
  getAccounts(customerReference?: string) {
    let params = new HttpParams();
    if (customerReference) params = params.set('customerReference', customerReference);
    return this.http.get<Account[]>(`${this.api}/api/accounts`, { params });
  }
  getAccount(id: string) {
    return this.http.get<Account>(`${this.api}/api/accounts/${id}`);
  }
  createAccount(request: AccountRequest) {
    return this.http.post<Account>(`${this.api}/api/accounts`, request);
  }
  changeStatus(id: string, status: AccountStatus) {
    return this.http.put<Account>(`${this.api}/api/accounts/${id}/status`, null, { params: { status } });
  }

  // ----- transaction-service
  getTransactions(accountId: string) {
    return this.http.get<Transaction[]>(`${this.api}/api/transactions`, { params: { accountId } });
  }
  createTransaction(request: TransactionRequest) {
    return this.http.post<Transaction>(`${this.api}/api/transactions`, request);
  }
  transfer(request: TransferRequest) {
    return this.http.post<Transaction[]>(`${this.api}/api/transactions/transfer`, request);
  }

  // ----- chatbot-service
  chat(message: string, conversationId?: string) {
    return this.http.post<ChatResponse>(`${this.api}/api/chat`, { conversationId, message });
  }

  // ----- notification-service (événements Kafka)
  getEvents() {
    return this.http.get<ReceivedEvent[]>(`${this.api}/api/notifications/events`);
  }
}

/** Message lisible à partir d'une erreur HTTP (ProblemDetail.detail renvoyé par les services). */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'Service injoignable : vérifiez que la Gateway (8888) et les services sont démarrés.';
    return err.error?.detail ?? err.error?.message ?? `Erreur ${err.status} : ${err.statusText}`;
  }
  return String(err);
}
