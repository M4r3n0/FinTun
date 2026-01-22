import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-subsidy-admin',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="admin-subsidy">
        <h3>Manage Subsidies</h3>
        
        <div class="create-form">
            <h4>Create New Program</h4>
            <div class="form-row">
                <input placeholder="Program Name" [(ngModel)]="newProgram.name">
                <input type="number" placeholder="Amount (TND)" [(ngModel)]="newProgram.amountPerUser">
            </div>
            <textarea placeholder="Description" [(ngModel)]="newProgram.description"></textarea>
            <div class="form-row">
                <input type="number" placeholder="Max Age" [(ngModel)]="criteria.maxAge">
                <select [(ngModel)]="criteria.role">
                    <option value="">Any Role</option>
                    <option value="USER">User</option>
                    <option value="STUDENT">Student</option>
                    <option value="MERCHANT">Merchant</option>
                </select>
            </div>
            <button (click)="create()">Create Program</button>
        </div>

        <hr>

        <div class="list">
            <h4>Active Programs</h4>
            <table *ngIf="programs.length > 0">
                <tr>
                    <th>Name</th>
                    <th>Amount</th>
                    <th>Budget Remaining</th>
                    <th>Criteria</th>
                </tr>
                <tr *ngFor="let p of programs">
                    <td>{{ p.name }}</td>
                    <td>{{ p.amountPerUser }}</td>
                    <td>{{ p.remainingBudget }} / {{ p.totalBudget }}</td>
                    <td>{{ p.criteriaJson }}</td>
                </tr>
            </table>
        </div>
    </div>
  `,
    styles: [`
    .admin-subsidy { padding: 20px; background: white; border-radius: 8px; }
    .create-form { background: #f8f9fa; padding: 15px; border-radius: 5px; border: 1px solid #eee; }
    .form-row { display: flex; gap: 10px; margin-bottom: 10px; }
    input, select, textarea { padding: 8px; border: 1px solid #ddd; border-radius: 4px; flex: 1; }
    textarea { width: 100%; margin-bottom: 10px; height: 60px; }
    button { background: #27ae60; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
    table { width: 100%; margin-top: 15px; border-collapse: collapse; }
    th, td { text-align: left; padding: 10px; border-bottom: 1px solid #eee; }
  `]
})
export class SubsidyAdminComponent implements OnInit {
    programs: any[] = [];
    newProgram: any = {
        name: '', description: '', amountPerUser: 100, totalBudget: 1000000, remainingBudget: 1000000
    };
    criteria: any = { maxAge: 99, role: '' };

    constructor(private api: ApiService) { }

    ngOnInit() {
        this.load();
    }

    load() {
        this.api.getSubsidies().subscribe(res => this.programs = res);
    }

    create() {
        // Construct JSON
        const c: any = {};
        if (this.criteria.maxAge) c.maxAge = this.criteria.maxAge;
        if (this.criteria.role) c.role = this.criteria.role;

        this.newProgram.criteriaJson = JSON.stringify(c);

        this.api.createSubsidyProgram(this.newProgram).subscribe(() => {
            alert('Program Created');
            this.load();
            this.newProgram = { name: '', description: '', amountPerUser: 100, totalBudget: 1000000, remainingBudget: 1000000 };
        });
    }
}
