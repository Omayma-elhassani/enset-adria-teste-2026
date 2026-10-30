import { Routes } from '@angular/router';
import { AccountDetail } from './pages/account-detail/account-detail';
import { Accounts } from './pages/accounts/accounts';
import { Chat } from './pages/chat/chat';
import { Events } from './pages/events/events';
import { Transfer } from './pages/transfer/transfer';

export const routes: Routes = [
  { path: '', redirectTo: 'accounts', pathMatch: 'full' },
  { path: 'accounts', component: Accounts, title: 'Comptes' },
  { path: 'accounts/:id', component: AccountDetail, title: 'Détail du compte' },
  { path: 'transfer', component: Transfer, title: 'Virement' },
  { path: 'chat', component: Chat, title: 'Assistant' },
  { path: 'events', component: Events, title: 'Événements Kafka' },
  { path: '**', redirectTo: 'accounts' },
];
