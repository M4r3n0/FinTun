import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-kyc-review',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="kyc-container">
      <!-- List of Users -->
      <div class="user-list">
        <h3>Pending Review</h3>
        <ul>
          <li *ngFor="let user of pendingUsers" (click)="selectUser(user)" [class.selected]="selectedUser?.id == user.id">
            {{ user.fullName }} <br>
            <small>{{ user.nationalId }}</small>
          </li>
          <li *ngIf="pendingUsers.length === 0">No pending requests</li>
        </ul>
      </div>

      <!-- Review Detail -->
      <div class="review-panel" *ngIf="selectedUser">
        <div class="header">
          <h2>{{ selectedUser.fullName }}</h2>
          <div>
            <button class="approve" (click)="decide(true)">Approve</button>
            <button class="reject" (click)="decide(false)">Reject</button>
          </div>
        </div>
        
        <div class="documents">
            <div *ngFor="let doc of documents" class="doc-card">
                <h4>{{ doc.type }}</h4>
                <div class="img-wrapper">
                    <!-- Simulate image display -->
                    <div class="placeholder-img">{{ doc.fileUrl }}</div>
                </div>
                <div class="ai-analysis">
                    <p>Status: <strong>{{ doc.status }}</strong></p>
                    <p>AI Score: <strong>{{ doc.confidenceScore * 100 | number:'1.0-1' }}%</strong></p>
                    <p *ngIf="doc.extractedData">OCR: {{ doc.extractedData }}</p>
                </div>
            </div>
            <div *ngIf="documents.length === 0">No documents uploaded</div>
        </div>
      </div>
      <div class="review-panel empty" *ngIf="!selectedUser">
        Select a user to review
      </div>
    </div>
  `,
  styles: [`
    .kyc-container { display: flex; height: 100%; gap: 20px; }
    .user-list { width: 250px; background: white; border-radius: 8px; padding: 15px; overflow-y: auto; }
    .user-list ul { list-style: none; padding: 0; }
    .user-list li { padding: 10px; border-bottom: 1px solid #eee; cursor: pointer; }
    .user-list li:hover, .user-list li.selected { background: #eef2f7; border-left: 4px solid #3498db; }
    
    .review-panel { flex: 1; background: white; border-radius: 8px; padding: 20px; display: flex; flex-direction: column; }
    .review-panel.empty { align-items: center; justify-content: center; color: #999; }
    
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 15px; }
    .documents { display: flex; flex-wrap: wrap; gap: 20px; overflow-y: auto; }
    .doc-card { border: 1px solid #ddd; padding: 10px; border-radius: 5px; width: 300px; }
    .img-wrapper { height: 150px; background: #eee; display: flex; align-items: center; justify-content: center; margin-bottom: 10px; overflow: hidden; }
    .placeholder-img { word-break: break-all; font-size: 0.8em; padding: 10px; }
    
    button { padding: 8px 15px; margin-left: 10px; cursor: pointer; border: none; border-radius: 4px; color: white; font-weight: bold; }
    button.approve { background: #2ecc71; }
    button.reject { background: #e74c3c; }
  `]
})
export class KycReviewComponent implements OnInit {
  pendingUsers: any[] = [];
  selectedUser: any = null;
  documents: any[] = [];

  constructor(private api: ApiService) { }

  ngOnInit() {
    this.refreshList();
  }

  refreshList() {
    this.api.getPendingKycUsers().subscribe(res => this.pendingUsers = res);
  }

  selectUser(user: any) {
    this.selectedUser = user;
    this.api.getUserKycDocs(user.id).subscribe(res => this.documents = res);
  }

  decide(approve: boolean) {
    if (!confirm(`Are you sure you want to ${approve ? 'Approve' : 'Reject'} this user?`)) return;

    this.api.reviewUserKyc(this.selectedUser.id, approve).subscribe(() => {
      alert('User ' + (approve ? 'Verified' : 'Rejected'));
      this.selectedUser = null;
      this.documents = [];
      this.refreshList();
    });
  }
}
