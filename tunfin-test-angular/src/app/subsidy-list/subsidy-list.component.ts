import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../services/api.service';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-subsidy-list',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="subsidy-container">
        <h3>Government Benefits</h3>
        
        <div class="programs-grid">
            <div *ngFor="let prog of programs" class="program-card">
                <div class="card-header">
                    <h4>{{ prog.name }}</h4>
                    <span class="amount">{{ prog.amountPerUser | number:'1.2-2' }} TND</span>
                </div>
                <p>{{ prog.description }}</p>
                <div class="criteria">
                    <small>Criteria: {{ prog.criteriaJson }}</small>
                </div>
                
                <div class="actions">
                    <button *ngIf="!eligibilityCaches[prog.id]" (click)="check(prog.id)">Check Eligibility</button>
                    
                    <div *ngIf="eligibilityCaches[prog.id] !== undefined">
                        <span *ngIf="eligibilityCaches[prog.id]" class="eligible-badge">Eligible</span>
                        <span *ngIf="!eligibilityCaches[prog.id]" class="not-eligible-badge">Not Eligible</span>
                        
                        <button *ngIf="eligibilityCaches[prog.id] && !hasClaimed(prog.id)" (click)="claim(prog.id)" class="claim-btn">Claim Now</button>
                        <button *ngIf="hasClaimed(prog.id)" disabled class="claimed-btn">Claimed</button>
                    </div>
                </div>
            </div>
        </div>

        <div *ngIf="programs.length === 0" class="empty">No active subsidy programs found.</div>
    </div>
  `,
    styles: [`
    .subsidy-container { padding: 20px; }
    .programs-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
    .program-card { background: white; border: 1px solid #e0e0e0; border-radius: 8px; padding: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
    .amount { font-weight: bold; color: #27ae60; font-size: 1.1em; }
    .actions { margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
    button { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; background: #34495e; color: white; }
    .claim-btn { background: #2980b9; }
    .claimed-btn { background: #95a5a6; cursor: default; }
    .eligible-badge { color: #27ae60; font-weight: bold; margin-right: 10px; }
    .not-eligible-badge { color: #e74c3c; font-weight: bold; margin-right: 10px; }
  `]
})
export class SubsidyListComponent implements OnInit {
    programs: any[] = [];
    claims: any[] = [];
    userId: string = '';
    eligibilityCaches: { [key: string]: boolean } = {};

    constructor(private api: ApiService) { }

    ngOnInit() {
        this.userId = this.api.getUserId() || '';
        this.loadData();
    }

    loadData() {
        this.api.getSubsidies().subscribe(res => this.programs = res);
        if (this.userId) {
            this.api.getUserClaims(this.userId).subscribe(res => this.claims = res);
        }
    }

    hasClaimed(programId: string): boolean {
        return this.claims.some(c => c.programId === programId);
    }

    check(programId: string) {
        if (!this.userId) return;
        this.api.checkEligibility(this.userId, programId).subscribe(res => {
            this.eligibilityCaches[programId] = res;
        });
    }

    claim(programId: string) {
        this.api.claimSubsidy(this.userId, programId).subscribe({
            next: (res) => {
                alert('Claim Successful! Funds deposited.');
                this.claims.push(res);
            },
            error: (err) => alert('Claim Failed: ' + err.error?.message)
        });
    }
}
