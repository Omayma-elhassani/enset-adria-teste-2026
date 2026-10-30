import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { BankApiService, errorMessage } from '../../core/bank-api.service';
import { Account, Transaction } from '../../core/models';

@Component({
  selector: 'app-transfer',
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './transfer.html',
})
export class Transfer {
  private readonly api = inject(BankApiService);
  private readonly fb = inject(FormBuilder);

  readonly from = signal<Account | null>(null);
  readonly to = signal<Account | null>(null);
  readonly result = signal<Transaction[]>([]);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    fromReference: ['CUST-001', Validators.required],
    toReference: ['CUST-002', Validators.required],
    amount: [500, [Validators.required, Validators.min(0.01)]],
  });

  /** Recherche le premier compte d'une référence client (ex : CUST-001, ACC0090). */
  lookup(which: 'from' | 'to') {
    const ref = this.form.controls[which === 'from' ? 'fromReference' : 'toReference'].value.trim();
    const target = which === 'from' ? this.from : this.to;
    target.set(null);
    if (!ref) return;
    this.api.getAccounts(ref).subscribe({
      next: list => {
        target.set(list[0] ?? null);
        this.error.set(list.length ? null : `Aucun compte pour la référence ${ref}`);
      },
      error: err => this.error.set(errorMessage(err)),
    });
  }

  submit() {
    const from = this.from();
    const to = this.to();
    if (!from || !to || this.form.invalid) {
      this.error.set('Recherchez d\'abord les deux comptes.');
      return;
    }
    this.api.transfer({
      fromAccountId: from.id, toAccountId: to.id,
      amount: this.form.controls.amount.value, currency: from.currency,
    }).subscribe({
      next: transactions => {
        this.result.set(transactions);
        this.error.set(null);
        this.lookup('from');
        this.lookup('to');
      },
      error: err => {
        this.error.set(errorMessage(err));
        this.result.set([]);
      },
    });
  }
}
