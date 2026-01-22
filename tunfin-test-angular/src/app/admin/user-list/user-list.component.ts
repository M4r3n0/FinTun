import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h3>User Management</h3>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Phone</th>
            <th>Role</th>
            <th>KYC Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let user of users">
            <td>{{ user.fullName }}</td>
            <td>{{ user.phoneNumber }}</td>
            <td><span class="badge" [ngClass]="user.role">{{ user.role }}</span></td>
            <td>
              <span class="badge" [ngClass]="user.kycLevel">{{ user.kycLevel }}</span>
            </td>
            <td>
              <button *ngIf="user.kycLevel === 'PENDING_VERIFICATION'" (click)="review(user)">Review</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
    table { width: 100%; border-collapse: collapse; margin-top: 15px; }
    th, td { text-align: left; padding: 12px; border-bottom: 1px solid #eee; }
    th { background: #f8f9fa; }
    .badge { padding: 4px 8px; border-radius: 4px; font-size: 0.85em; font-weight: bold; }
    .badge.ADMIN { background: #e74c3c; color: white; }
    .badge.USER { background: #3498db; color: white; }
    .badge.MERCHANT { background: #9b59b6; color: white; }
    .badge.VERIFIED { background: #2ecc71; color: white; }
    .badge.PENDING_VERIFICATION { background: #f1c40f; color: black; }
    .badge.UNVERIFIED { background: #95a5a6; color: white; }
    button { padding: 5px 10px; cursor: pointer; background: #34495e; color: white; border: none; border-radius: 4px; }
  `]
})
export class UserListComponent implements OnInit {
  users: any[] = [];

  constructor(private api: ApiService) { }

  ngOnInit() {
    this.api.getUsers().subscribe({
      next: (res) => this.users = res,
      error: (err) => console.error(err)
    });
  }

  review(user: any) {
    alert('Go to KYC Review tab to approve ' + user.fullName);
  }
}
