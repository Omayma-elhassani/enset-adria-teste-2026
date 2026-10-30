import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { BankApiService, errorMessage } from '../../core/bank-api.service';
import { ReceivedEvent } from '../../core/models';

/** Événements TransactionCreated reçus par notification-service depuis Kafka (rafraîchis toutes les 5 s). */
@Component({
  selector: 'app-events',
  imports: [DatePipe, DecimalPipe],
  templateUrl: './events.html',
})
export class Events {
  private readonly api = inject(BankApiService);

  readonly events = signal<ReceivedEvent[]>([]);
  readonly error = signal<string | null>(null);
  readonly lastRefresh = signal<Date | null>(null);

  constructor() {
    this.refresh();
    const timer = setInterval(() => this.refresh(), 5000);
    inject(DestroyRef).onDestroy(() => clearInterval(timer));
  }

  refresh() {
    this.api.getEvents().subscribe({
      next: list => {
        this.events.set(list);
        this.error.set(null);
        this.lastRefresh.set(new Date());
      },
      error: err => this.error.set(errorMessage(err)),
    });
  }
}
