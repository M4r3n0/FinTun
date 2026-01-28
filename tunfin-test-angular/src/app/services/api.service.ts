import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private token: string | null = null;
  private userId: string | null = null;

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

  setUserId(userId: string) {
    this.userId = userId;
    localStorage.setItem('userId', userId);
  }

  getUserId(): string | null {
    if (!this.userId) {
      this.userId = localStorage.getItem('userId');
    }
    return this.userId;
  }

  clearAuth() {
    this.token = null;
    this.userId = null;
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
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

  getTransactionHistory(accountId: string): Observable<any> {
    return this.http.get(`/api/wallet/accounts/${accountId}/transactions`, { headers: this.getHeaders() });
  }

  // Payment
  transfer(senderId: string, receiverId: string, amount: number): Observable<any> {
    return this.http.post('/api/payment/p2p', { senderAccountId: senderId, receiverAccountId: receiverId, amount }, { headers: this.getHeaders() });
  }

  // Admin
  getUsers(): Observable<any> {
    return this.http.get('/api/admin/users', { headers: this.getHeaders() });
  }

  getPendingKycUsers(): Observable<any> {
    return this.http.get('/api/admin/kyc/pending', { headers: this.getHeaders() });
  }

  // KYC
  uploadKycDoc(userId: string, type: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('userId', userId);
    formData.append('type', type);
    formData.append('file', file);
    // Don't set Content-Type header for Multipart, browser sets it with boundary
    let headers = new HttpHeaders();
    const token = this.getToken();
    if (token) headers = headers.set('Authorization', `Bearer ${token}`);

    return this.http.post('/api/kyc/upload', formData, { headers });
  }

  getUserKycDocs(userId: string): Observable<any> {
    return this.http.get(`/api/kyc/documents?userId=${userId}`, { headers: this.getHeaders() });
  }

  reviewUserKyc(userId: string, approve: boolean): Observable<any> {
    return this.http.post(`/api/kyc/review/${userId}?approve=${approve}`, {}, { headers: this.getHeaders() });
  }

  // --- Dispute Resolution API ---
  fileDispute(paymentId: string, reason: string): Observable<any> {
    return this.http.post('/api/disputes', { paymentId, reason }, { headers: this.getHeaders() });
  }

  getMyDisputes(): Observable<any[]> {
    return this.http.get<any[]>('/api/disputes/my-disputes', { headers: this.getHeaders() });
  }

  getAllDisputes(): Observable<any[]> {
    return this.http.get<any[]>('/api/disputes/admin/all', { headers: this.getHeaders() });
  }

  resolveDispute(disputeId: string, status: string): Observable<any> {
    return this.http.post(`/api/disputes/admin/resolve/${disputeId}?status=${status}`, {}, { headers: this.getHeaders() });
  }

  // Subsidies
  getSubsidies(): Observable<any> {
    return this.http.get('/api/subsidies', { headers: this.getHeaders() });
  }

  createSubsidyProgram(program: any): Observable<any> {
    return this.http.post('/api/subsidies/programs', program, { headers: this.getHeaders() });
  }

  checkEligibility(userId: string, programId: string): Observable<any> {
    return this.http.get(`/api/subsidies/eligibility?userId=${userId}&programId=${programId}`, { headers: this.getHeaders() });
  }

  claimSubsidy(userId: string, programId: string): Observable<any> {
    return this.http.post(`/api/subsidies/claim?userId=${userId}&programId=${programId}`, {}, { headers: this.getHeaders() });
  }

  getUserClaims(userId: string): Observable<any> {
    return this.http.get(`/api/subsidies/claims?userId=${userId}`, { headers: this.getHeaders() });
  }
}

