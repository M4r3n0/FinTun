import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './dashboard.component.html',
    styles: [`
    .container { max-width: 800px; margin: 20px auto; padding: 20px; }
    .section { border: 1px solid #eee; padding: 15px; margin-bottom: 20px; border-radius: 5px; }
    .balance { font-size: 2em; font-weight: bold; }
    button { padding: 5px 10px; margin: 5px; cursor: pointer; }
    input { padding: 5px; margin: 5px; }
  `]
})
export class DashboardComponent implements OnInit {
    accounts: any[] = [];
    userId: string = ''; // Ideally decode from token

    // Create Account Form
    newAccountCurrency = 'TND';

    // Transfer Form
    transferReceiverId = '';
    transferAmount = 0;
    selectedSourceAccount = '';

    message = '';
    error = '';

    constructor(private api: ApiService, private router: Router) { }

    ngOnInit() {
        this.decodeUser();
        if (this.userId) {
            this.fetchAccounts();
        }
    }

    decodeUser() {
        const token = this.api.getToken();
        if (!token) {
            this.router.navigate(['/login']);
            return;
        }
        // Basic decode payload
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            this.userId = payload.sub; // PhoneNumber in our case? Or we need ID.
            // Wait, Identity Service uses UUID for User ID but JWT subject is PhoneNumber.
            // We need the User UUID to create an account.
            // The Token should ideally contain the User ID.
            // Let's assume for this test app we need to fetch User ID or it's in claims.
            // If not in claims, we might need a /me endpoint or update backend.
            // Assuming user knows their ID or we add it to token (backend change required?)
            // Let's check backend... JwtUtil uses UserDetails username.
            // We might be blocked here unless we update backend to include userId in token.
        } catch (e) {
            console.error(e);
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
            // Fallback: Ask user for UUID if we can't decode it
            const input = prompt("Please enter your User UUID (from DB/Logs):");
            if (input) this.userId = input;
            else return;
        }

        this.api.createAccount(this.userId, this.newAccountCurrency).subscribe({
            next: (res) => {
                this.message = 'Account Created!';
                this.accounts.push(res);
                this.selectedSourceAccount = res.id; // Auto-select
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
                // Refund balance? We need to refresh account.
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
        this.api.setToken('');
        this.router.navigate(['/login']);
    }
}
