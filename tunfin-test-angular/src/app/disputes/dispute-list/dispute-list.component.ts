import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-dispute-list',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="dispute-container">
      <h2>My Payment Disputes</h2>
      
      <div class="dispute-card" *ngFor="let dispute of disputes">
        <div class="dispute-header">
          <span class="category">{{ dispute.category }}</span>
          <span class="status" [ngClass]="dispute.status.toLowerCase()">{{ dispute.status }}</span>
        </div>
        <p class="reason">{{ dispute.reason }}</p>
        <div class="meta">
          <span>ID: {{ dispute.id.substring(0,8) }}</span>
          <span>Created: {{ dispute.createdAt | date:'short' }}</span>
        </div>
        <div class="ai-insight" *ngIf="dispute.aiRecommendation">
          <strong>AI Analysis:</strong> {{ dispute.aiRecommendation }}
        </div>
      </div>

      <div *ngIf="disputes.length === 0" class="empty-state">
        No disputes filed.
      </div>
    </div>
  `,
    styles: [`
    .dispute-container { padding: 20px; color: #fff; }
    .dispute-card { 
      background: rgba(255,255,255,0.05); 
      border: 1px solid rgba(255,255,255,0.1);
      padding: 15px;
      margin-bottom: 15px;
      border-radius: 8px;
    }
    .dispute-header { display: flex; justify-content: space-between; margin-bottom: 10px; }
    .category { font-weight: bold; color: #4facfe; }
    .status { padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; }
    .pending { background: #f39c12; }
    .auto_resolved { background: #27ae60; }
    .escalated { background: #e74c3c; }
    .reason { margin: 10px 0; font-style: italic; }
    .meta { font-size: 0.75rem; color: #aaa; display: flex; gap: 15px; }
    .ai-insight { 
      margin-top: 10px; 
      padding: 10px; 
      background: rgba(79, 172, 254, 0.1); 
      border-left: 3px solid #4facfe;
      font-size: 0.85rem;
    }
  `]
})
export class DisputeListComponent implements OnInit {
    disputes: any[] = [];

    constructor(private apiService: ApiService) { }

    ngOnInit(): void {
        this.loadDisputes();
    }

    loadDisputes() {
        this.apiService.getMyDisputes().subscribe(res => {
            this.disputes = res;
        });
    }
}
