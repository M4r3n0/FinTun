import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './login.component.html',
    styles: [`
    .container { max-width: 400px; margin: 50px auto; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
    .form-group { margin-bottom: 15px; }
    label { display: block; margin-bottom: 5px; }
    input { width: 100%; padding: 8px; box-sizing: border-box; }
    button { width: 100%; padding: 10px; background-color: #007bff; color: white; border: none; cursor: pointer; }
    button:hover { background-color: #0056b3; }
    .error { color: red; }
    .toggle { text-align: center; margin-top: 10px; cursor: pointer; color: blue; }
  `]
})
export class LoginComponent {
    isRegister = false;
    phoneNumber = '';
    password = '';
    fullName = ''; // Register only
    nationalId = ''; // Register only
    email = ''; // Register only
    address = ''; // Register only
    dateOfBirth = ''; // Register only
    error = '';

    constructor(private api: ApiService, private router: Router) { }

    toggleMode() {
        this.isRegister = !this.isRegister;
        this.error = '';
    }

    onSubmit() {
        this.error = '';
        if (this.isRegister) {
            this.api.register({
                phoneNumber: this.phoneNumber,
                password: this.password,
                fullName: this.fullName,
                nationalId: this.nationalId,
                email: this.email,
                address: this.address,
                dateOfBirth: this.dateOfBirth
            }).subscribe({
                next: (res) => {
                    this.api.setToken(res.token);
                    if (res.userId) {
                        this.api.setUserId(res.userId);
                    }
                    this.router.navigate(['/dashboard']);
                },
                error: (err) => this.error = 'Registration Failed: ' + (err.error?.message || err.statusText)
            });
        } else {
            this.api.login({
                phoneNumber: this.phoneNumber,
                password: this.password
            }).subscribe({
                next: (res) => {
                    this.api.setToken(res.token);
                    if (res.userId) {
                        this.api.setUserId(res.userId);
                    }
                    this.router.navigate(['/dashboard']);
                },
                error: (err) => this.error = 'Login Failed: ' + (err.error?.message || err.statusText)
            });
        }
    }
}
