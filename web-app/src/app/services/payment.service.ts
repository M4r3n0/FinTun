import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TransferRequest {
  senderAccountId: string;
  receiverAccountId: string;
  amount: number;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = 'http://localhost:8083/api/payment';

  constructor(private http: HttpClient) { }

  transfer(request: TransferRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/p2p`, request);
  }
}
