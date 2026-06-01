import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  FeeStructureDto,
  FeePaymentDto,
  StudentFeesSummaryDto,
  CreateFeeStructureRequest,
  RecordPaymentRequest
} from '../models/fee.model';

@Injectable({ providedIn: 'root' })
export class FeeService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listStructures(): Observable<FeeStructureDto[]> {
    return this.http.get<FeeStructureDto[]>(`${this.base}/fees/structures`);
  }

  createStructure(req: CreateFeeStructureRequest): Observable<FeeStructureDto> {
    return this.http.post<FeeStructureDto>(`${this.base}/fees/structures`, req);
  }

  deleteStructure(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/fees/structures/${id}`);
  }

  recordPayment(req: RecordPaymentRequest): Observable<FeePaymentDto> {
    return this.http.post<FeePaymentDto>(`${this.base}/fees/pay`, req);
  }

  getStudentHistory(studentId: string): Observable<FeePaymentDto[]> {
    return this.http.get<FeePaymentDto[]>(`${this.base}/fees/student/${studentId}/history`);
  }

  getStudentSummary(studentId: string): Observable<StudentFeesSummaryDto> {
    return this.http.get<StudentFeesSummaryDto>(`${this.base}/fees/student/${studentId}/summary`);
  }
}
