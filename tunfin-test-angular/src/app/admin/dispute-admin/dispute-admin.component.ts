import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
    selector: 'app-dispute-admin',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="admin-dispute-container">
      <h2>Dispute Resolution Center (AI-Powered)</h2>
      
      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>Status</th>
              <th>Category</th>
              <th>Reason</th>
              <th>AI Confidence</th>
              <th>AI Recommendation</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let d of disputes">
              <td><span class="status-badge" [ngClass]="d.status.toLowerCase()">{{ d.status }}</span></td>
              <td>{{ d.category }}</td>
              <td class="reason-cell">{{ d.reason }}</td>
              <td>
                <div class="conf-bar">
                  <div class="conf-fill" [style.width.%]="d.aiConfidence * 100"></div>
                  <span>{{ (d.aiConfidence * 100).toFixed(0) }}%</span>
                </div>
              </td>
              <td class="rec-cell">{{ d.aiRecommendation }}</td>
              <td>
                <div class="actions" *ngIf="d.status === 'ESCALATED' || d.status === 'PENDING'">
                  <button class="btn approve" (click)="resolve(d.id, 'RESOLVED')">Approve Refund</button>
                  <button class="btn reject" (click)="resolve(d.id, 'REJECTED')">Reject</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
    styles: [`
    .admin-dispute-container { padding: 20px; color: #fff; }
    .table-container { overflow-x: auto; background: rgba(0,0,0,0.2); border-radius: 8px; }
    table { width: 100%; border-collapse: collapse; }
    th, td { padding: 12px; text-align: left; border-bottom: 1px solid rgba(255,255,255,0.1); }
    .status-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.75rem; }
    .pending { background: #f39c12; }
    .auto_resolved { background: #27ae60; }
    .escalated { background: #e74c3c; }
    .resolved { background: #2ecc71; }
    .rejected { background: #95a5a6; }
    .conf-bar { background: #333; height: 15px; border-radius: 10px; position: relative; width: 80px; }
    .conf-fill { background: #4facfe; height: 100%; border-radius: 10px; }
    .conf-bar span { position: absolute; right: -40px; top: -2px; font-size: 0.7rem; }
    .reason-cell, .rec-cell { max-width: 200px; font-size: 0.85rem; }
    .actions { display: flex; gap: 5px; }
    .btn { padding: 5px 10px; border: none; border-radius: 4px; cursor: pointer; font-size: 0.75rem; }
    .approve { background: #2ecc71; color: white; }
    .reject { background: #e74c3c; color: white; }
  `]
})
export class DisputeAdminComponent implements OnInit {
    disputes: any[] = [];

    constructor(private apiService: ApiService) { }

    ngOnInit(): void {
        this.loadAll();
    }

    loadAll() {
        this.apiService.getAllDisputes().subscribe(res => {
            this.disputes = res;
        });
    }

    resolve(id: string, status: string) {
        this.apiService.resolveDispute(id, status).subscribe(() => {
            this.loadAll();
        });
    }
}
