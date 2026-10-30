import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BankApiService, errorMessage } from '../../core/bank-api.service';

interface ChatMessage {
  role: 'user' | 'assistant' | 'error';
  text: string;
}

@Component({
  selector: 'app-chat',
  imports: [FormsModule],
  templateUrl: './chat.html',
})
export class Chat {
  private readonly api = inject(BankApiService);
  private readonly scroller = viewChild<ElementRef<HTMLElement>>('scroller');

  readonly messages = signal<ChatMessage[]>([
    { role: 'assistant', text: 'Bonjour ! Je suis l\'assistant bancaire. Posez-moi une question sur les comptes ou les transactions.' },
  ]);
  readonly pending = signal(false);
  readonly suggestions = [
    'Quel est le solde des comptes du client CUST-001 ?',
    'Quelles sont les dernières transactions du client ACC0090 ?',
    'Bloque le compte du client CUST-002',
    'Fais un virement de 200 MAD de CUST-001 vers CUST-002',
  ];
  draft = '';
  private conversationId?: string;

  send(text = this.draft) {
    const message = text.trim();
    if (!message || this.pending()) return;
    this.draft = '';
    this.messages.update(m => [...m, { role: 'user', text: message }]);
    this.pending.set(true);
    this.scrollSoon();
    this.api.chat(message, this.conversationId).subscribe({
      next: res => {
        this.conversationId = res.conversationId;
        this.messages.update(m => [...m, { role: 'assistant', text: res.answer }]);
        this.pending.set(false);
        this.scrollSoon();
      },
      error: err => {
        this.messages.update(m => [...m, { role: 'error', text: errorMessage(err) }]);
        this.pending.set(false);
        this.scrollSoon();
      },
    });
  }

  reset() {
    this.conversationId = undefined;
    this.messages.set([{ role: 'assistant', text: 'Nouvelle conversation. Comment puis-je vous aider ?' }]);
  }

  private scrollSoon() {
    setTimeout(() => {
      const el = this.scroller()?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }
}
