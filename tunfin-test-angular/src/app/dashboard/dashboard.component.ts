import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="container">
      <header>
        <h1>TunFin Wallet</h1>
        <button (click)="logout()">Logout</button>
      </header>
      
      <!-- KYC Section -->
      <div class="section kyc" [ngClass]="kycStatus">
        <h3>Identity Verification</h3>
        <p>Status: <strong>{{ kycStatus }}</strong></p>
        
        <div *ngIf="kycStatus === 'UNVERIFIED'">
             <div class="upload-group">
                <label>Upload ID Front:</label>
                <input type="file" (change)="onFileSelected($event, 'ID_CARD_FRONT')">
             </div>
             <div class="upload-group">
                <label>Upload Selfie:</label>
                <input type="file" (change)="onFileSelected($event, 'SELFIE')">
             </div>
        </div>
        <div *ngIf="kycStatus === 'PENDING'">
            <p>Your documents are under review. (AI Check in progress)</p>
        </div>
      </div>

      <!-- Wallet Section -->
      <div class="section" *ngIf="kycStatus === 'VERIFIED' || kycStatus === 'UNVERIFIED'"> 
         <!-- Allow access for now, but usually restricted -->
         <h2>Your Accounts</h2>
         <div *ngFor="let acc of accounts" class="account-card">
            <p>{{ acc.currency }} Balance: <span class="balance">{{ acc.balance | number:'1.2-2' }}</span></p>
         </div>
      </div>
      
      <!-- Actions -->
      <div class="section">
         <button (click)="createAccount()">Create Wallet</button>
         <h3>Quick Transfer</h3>
         <input placeholder="Receiver ID (UUID)" [(ngModel)]="transferReceiverId">
         <input type="number" placeholder="Amount" [(ngModel)]="transferAmount">
         <button (click)="transfer()">Send Money</button>
         <p class="error">{{ error }}</p>
         <p class="success">{{ message }}</p>
      </div>
    </div>
    `,
    styles: [`
    .container { max-width: 800px; margin: 20px auto; padding: 20px; }
    .section { border: 1px solid #eee; padding: 15px; margin-bottom: 20px; border-radius: 5px; }
    .balance { font-size: 2em; font-weight: bold; }
    button { padding: 5px 10px; margin: 5px; cursor: pointer; }
    input { padding: 5px; margin: 5px; }
    .kyc { border-left: 5px solid #ccc; }
    .kyc.VERIFIED { border-left-color: #2ecc71; }
    .kyc.PENDING { border-left-color: #f1c40f; }
    .kyc.UNVERIFIED { border-left-color: #95a5a6; }
  `]
})
export class DashboardComponent implements OnInit {
    accounts: any[] = [];
    userId: string = '';

    // Create Account Form
    newAccountCurrency = 'TND';

    // Transfer Form
    transferReceiverId = '';
    transferAmount = 0;
    selectedSourceAccount = '';

    message = '';
    error = '';

    // KYC
    kycDocs: any[] = [];
    kycStatus = 'UNVERIFIED';

    constructor(private api: ApiService, private router: Router) { }

    ngOnInit() {
        this.decodeUser();
        if (this.userId) {
            this.fetchAccounts();
            this.fetchKycStatus();
        }
    }

    decodeUser() {
        const token = this.api.getToken();
        if (!token) {
            this.router.navigate(['/login']);
            return;
        }

        // First try to get userId from localStorage (set during login)
        const storedUserId = this.api.getUserId();
        if (storedUserId) {
            this.userId = storedUserId;
            return;
        }

        // Fallback: decode from JWT token (userId claim added in backend)
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            this.userId = payload.userId || payload.sub;
        } catch (e) {
            console.error('Failed to decode token:', e);
        }
    }

    fetchKycStatus() {
        this.api.getUserKycDocs(this.userId).subscribe({
            next: (res) => {
                this.kycDocs = res;
                if (this.kycDocs.some(d => d.status === 'VERIFIED')) this.kycStatus = 'VERIFIED';
                else if (this.kycDocs.some(d => d.status === 'PENDING')) this.kycStatus = 'PENDING';
                else if (this.kycDocs.length > 0) this.kycStatus = 'PENDING';
            }
        });
    }

    onFileSelected(event: any, type: string) {
        const file = event.target.files[0];
        if (file) {
            this.api.uploadKycDoc(this.userId, type, file).subscribe({
                next: (res) => {
                    alert('Upload Successful');
                    this.fetchKycStatus();
                },
                error: (err) => alert('Upload Failed')
            });
        }
    }

    fetchAccounts() {
        this.api.getAccountsByUser(this.userId).subscribe({
            next: (res) => {
                this.accounts = res;
                if (this.accounts.length > 0) {
                    this.selectedSourceAccount = this.accounts[0].id;
                }
            },
            error: (err) => this.error = 'Fetch Accounts Failed: ' + (err.error?.message || err.statusText)
        });
    }

    createAccount() {
        if (!this.userId) {
            const input = prompt("Please enter your User UUID (from DB/Logs):");
            if (input) this.userId = input;
            else return;
        }

        this.api.createAccount(this.userId, this.newAccountCurrency).subscribe({
            next: (res) => {
                this.message = 'Account Created!';
                this.accounts.push(res);
                this.selectedSourceAccount = res.id;
            },
            error: (err) => this.error = 'Create Account Failed: ' + (err.error?.message || err.statusText)
        });
    }

    transfer() {
        if (!this.selectedSourceAccount || !this.transferReceiverId || !this.transferAmount) {
            this.error = 'Please fill all fields';
            return;
        }

        this.api.transfer(this.selectedSourceAccount, this.transferReceiverId, this.transferAmount).subscribe({
            next: (res) => {
                this.message = 'Transfer Successful! Status: ' + res.status;
                this.refreshAccount(this.selectedSourceAccount);
            },
            error: (err) => this.error = 'Transfer Failed: ' + (err.error?.message || err.statusText)
        });
    }

    refreshAccount(id: string) {
        this.api.getAccount(id).subscribe(res => {
            const index = this.accounts.findIndex(a => a.id === id);
            if (index !== -1) {
                this.accounts[index] = res;
            }
        });
    }

    logout() {
        this.api.clearAuth();
        this.router.navigate(['/login']);
    }
}
