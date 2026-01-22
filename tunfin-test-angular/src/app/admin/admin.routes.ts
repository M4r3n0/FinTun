import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { UserListComponent } from './user-list/user-list.component';
import { KycReviewComponent } from './kyc-review/kyc-review.component';

export const ADMIN_ROUTES: Routes = [
    {
        path: '',
        component: AdminLayoutComponent,
        children: [
            { path: '', redirectTo: 'users', pathMatch: 'full' },
            { path: 'users', component: UserListComponent },
            { path: 'kyc', component: KycReviewComponent },
            { path: 'subsidies', loadComponent: () => import('./subsidy-admin/subsidy-admin.component').then(m => m.SubsidyAdminComponent) }
        ]
    }
];
