import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AttendanceRecordDto,
  AttendanceSummaryDto,
  MarkAttendanceRequest
} from '../models/attendance.model';

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  mark(req: MarkAttendanceRequest): Observable<AttendanceRecordDto[]> {
    return this.http.post<AttendanceRecordDto[]>(`${this.base}/attendance/mark`, req);
  }

  getClassRoll(classId: string, date: string): Observable<AttendanceRecordDto[]> {
    return this.http.get<AttendanceRecordDto[]>(
      `${this.base}/attendance/class/${classId}`,
      { params: new HttpParams().set('date', date) }
    );
  }

  getStudentHistory(studentId: string, from: string, to: string): Observable<AttendanceRecordDto[]> {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<AttendanceRecordDto[]>(
      `${this.base}/attendance/student/${studentId}/history`, { params });
  }

  getStudentSummary(studentId: string, from?: string, to?: string): Observable<AttendanceSummaryDto> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);
    return this.http.get<AttendanceSummaryDto>(
      `${this.base}/attendance/student/${studentId}/summary`, { params });
  }
}
