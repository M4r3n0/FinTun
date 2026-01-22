import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private token: string | null = null;

  constructor(private http: HttpClient) { }

  setToken(token: string) {
    this.token = token;
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    if (!this.token) {
      this.token = localStorage.getItem('token');
    }
    return this.token;
  }

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    const token = this.getToken();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  // Identity
  register(data: any): Observable<any> {
    return this.http.post('/api/auth/register', data);
  }

  login(data: any): Observable<any> {
    return this.http.post('/api/auth/login', data);
  }

  // Wallet
  createAccount(userId: string, currency: string = 'TND'): Observable<any> {
    return this.http.post('/api/wallet/accounts', { userId, currency }, { headers: this.getHeaders() });
  }

  getAccount(id: string): Observable<any> {
    return this.http.get(`/api/wallet/accounts/${id}`, { headers: this.getHeaders() });
  }

  getAccountsByUser(userId: string): Observable<any> {
    return this.http.get(`/api/wallet/accounts/user/${userId}`, { headers: this.getHeaders() });
  }

  // Payment
  transfer(senderId: string, receiverId: string, amount: number): Observable<any> {
    return this.http.post('/api/payment/p2p', { senderAccountId: senderId, receiverAccountId: receiverId, amount }, { headers: this.getHeaders() });
  }
}
