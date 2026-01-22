import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="admin-container">
      <aside class="sidebar">
        <h2>Admin Portal</h2>
        <nav>
          <a routerLink="/admin/users" routerLinkActive="active">Users</a>
          <a routerLink="/admin/kyc" routerLinkActive="active">KYC Review</a>
          <a routerLink="/dashboard">Back to App</a>
        </nav>
      </aside>
      <main class="content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .admin-container { display: flex; height: 100vh; }
    .sidebar { width: 250px; background: #2c3e50; color: white; padding: 20px; }
    .sidebar h2 { margin-bottom: 20px; color: #ecf0f1; }
    .sidebar nav a { display: block; padding: 10px; color: #bdc3c7; text-decoration: none; }
    .sidebar nav a.active { background: #34495e; color: white; border-left: 4px solid #3498db; }
    .content { flex: 1; padding: 20px; overflow-y: auto; background: #f5f6fa; }
  `]
})
export class AdminLayoutComponent { }
