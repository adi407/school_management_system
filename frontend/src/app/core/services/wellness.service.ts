import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClassPulseDto, WellnessCheckinRequest } from '../models/wellness.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WellnessService {
  private base = `${environment.apiUrl}/wellness`;

  constructor(private http: HttpClient) {}

  /** Student submits today's mood */
  checkin(req: WellnessCheckinRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/checkin`, req);
  }

  /** Teacher / counselor: pulse for one class */
  getClassPulse(classId: string, date?: string): Observable<ClassPulseDto> {
    let p = new HttpParams();
    if (date) p = p.set('date', date);
    return this.http.get<ClassPulseDto>(`${this.base}/pulse/class/${classId}`, { params: p });
  }

  /** Admin / counselor: pulse for all classes in school */
  getSchoolPulse(date?: string): Observable<ClassPulseDto[]> {
    let p = new HttpParams();
    if (date) p = p.set('date', date);
    return this.http.get<ClassPulseDto[]>(`${this.base}/pulse/school`, { params: p });
  }

  /** Mood trend for a class over a date range */
  getClassTrend(classId: string, from: string, to: string): Observable<ClassPulseDto[]> {
    const p = new HttpParams().set('from', from).set('to', to);
    return this.http.get<ClassPulseDto[]>(`${this.base}/trend/${classId}`, { params: p });
  }
}
