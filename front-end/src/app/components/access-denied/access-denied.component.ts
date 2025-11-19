import { Component } from '@angular/core';

@Component({
  selector: 'app-access-denied',
  standalone: true,
  template: `
    <div class="access-denied-container">
      <h1>Accès refusé</h1>
      <p>Vous n'avez pas la permission d'accéder à cette page.</p>
      <a routerLink="/login">Se connecter</a>
    </div>
  `,
  styles: [`
    .access-denied-container {
      text-align: center;
      margin-top: 100px;
    }
    h1 {
      color: #d32f2f;
      font-size: 2.5rem;
      margin-bottom: 1rem;
    }
    p {
      font-size: 1.2rem;
      margin-bottom: 2rem;
    }
    a {
      color: #1976d2;
      text-decoration: underline;
      font-weight: bold;
    }
  `]
})
export class AccessDeniedComponent {}
