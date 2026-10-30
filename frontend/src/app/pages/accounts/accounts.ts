import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BankApiService, errorMessage } from '../../core/bank-api.service';
import { Account } from '../../core/models';

@Component({
  selector: 'app-accounts',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './accounts.html',
})
export class Accounts {
  private readonly api = inject(BankApiService);
  private readonly fb = inject(FormBuilder);

  readonly pageSize = 15;
  readonly accounts = signal<Account[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly search = signal('');
  readonly statusFilter = signal<'ALL' | 'ACTIVE' | 'BLOCKED'>('ALL');
  readonly page = signal(0);
  readonly showForm = signal(false);

  readonly filtered = computed(() => {
    const q = this.search().trim().toLowerCase();
    const status = this.statusFilter();
    return this.accounts().filter(a =>
      (status === 'ALL' || a.status === status) &&
      (!q || a.customerReference.toLowerCase().includes(q) || a.id.includes(q)));
  });
  readonly pageCount = computed(() => Math.max(1, Math.ceil(this.filtered().length / this.pageSize)));
  readonly pageItems = computed(() =>
    this.filtered().slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize));
  readonly activeCount = computed(() => this.accounts().filter(a => a.status === 'ACTIVE').length);
  readonly blockedCount = computed(() => this.accounts().filter(a => a.status === 'BLOCKED').length);

  readonly form = this.fb.nonNullable.group({
    customerReference: ['', Validators.required],
    currency: ['MAD', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    initialBalance: [0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.api.getAccounts().subscribe({
      next: accounts => {
        this.accounts.set(accounts);
        this.loading.set(false);
      },
      error: err => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  onSearch(value: string) {
    this.search.set(value);
    this.page.set(0);
  }

  onStatus(value: string) {
    this.statusFilter.set(value as 'ALL' | 'ACTIVE' | 'BLOCKED');
    this.page.set(0);
  }

  create() {
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    this.api.createAccount({ ...value, currency: value.currency.toUpperCase() }).subscribe({
      next: account => {
        this.accounts.update(list => [account, ...list]);
        this.success.set(`Compte créé pour ${account.customerReference}`);
        this.error.set(null);
        this.form.reset({ customerReference: '', currency: 'MAD', initialBalance: 0 });
        this.showForm.set(false);
      },
      error: err => this.error.set(errorMessage(err)),
    });
  }
}
