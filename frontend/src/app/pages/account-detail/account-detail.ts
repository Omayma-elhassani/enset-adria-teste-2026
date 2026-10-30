import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BankApiService, errorMessage } from '../../core/bank-api.service';
import { Account, Transaction, TransactionType } from '../../core/models';

@Component({
  selector: 'app-account-detail',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe, DatePipe],
  templateUrl: './account-detail.html',
})
export class AccountDetail implements OnInit {
  private readonly api = inject(BankApiService);
  private readonly fb = inject(FormBuilder);

  /** Paramètre de route :id (withComponentInputBinding). */
  readonly id = input.required<string>();

  readonly account = signal<Account | null>(null);
  readonly transactions = signal<Transaction[]>([]);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly totalDebit = computed(() =>
    this.transactions().filter(t => t.type === 'DEBIT').reduce((s, t) => s + Number(t.amount), 0));
  readonly totalCredit = computed(() =>
    this.transactions().filter(t => t.type === 'CREDIT').reduce((s, t) => s + Number(t.amount), 0));

  readonly categories = ['GROCERY', 'RESTAURANT', 'ECOMMERCE', 'ELECTRONICS', 'TRANSPORT', 'TRAVEL'];

  readonly form = this.fb.nonNullable.group({
    type: ['DEBIT' as TransactionType, Validators.required],
    amount: [100, [Validators.required, Validators.min(0.01)]],
    merchantCategory: ['GROCERY'],
    country: ['MA'],
  });

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.api.getAccount(this.id()).subscribe({
      next: a => this.account.set(a),
      error: err => this.error.set(errorMessage(err)),
    });
    this.api.getTransactions(this.id()).subscribe({
      next: list => this.transactions.set(list),
      error: err => this.error.set(errorMessage(err)),
    });
  }

  toggleStatus() {
    const a = this.account();
    if (!a) return;
    this.api.changeStatus(a.id, a.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE').subscribe({
      next: updated => {
        this.account.set(updated);
        this.success.set(`Compte ${updated.status === 'ACTIVE' ? 'activé' : 'bloqué'}`);
        this.error.set(null);
      },
      error: err => this.error.set(errorMessage(err)),
    });
  }

  submit() {
    const a = this.account();
    if (!a || this.form.invalid) return;
    const v = this.form.getRawValue();
    this.api.createTransaction({
      accountId: a.id, currency: a.currency, type: v.type, amount: v.amount,
      merchantCategory: v.merchantCategory || undefined, country: v.country || undefined,
    }).subscribe({
      next: t => {
        this.success.set(`Transaction ${t.type} de ${t.amount} ${t.currency} enregistrée`);
        this.error.set(null);
        this.reload();
      },
      error: err => {
        this.error.set(errorMessage(err));
        this.success.set(null);
      },
    });
  }
}
