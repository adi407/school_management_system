import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TimetableSlotDto, UpsertSlotRequest } from '../models/timetable.model';

@Injectable({ providedIn: 'root' })
export class TimetableService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getClassTimetable(classId: string, academicYearId: string): Observable<TimetableSlotDto[]> {
    const params = new HttpParams().set('academicYearId', academicYearId);
    return this.http.get<TimetableSlotDto[]>(`${this.base}/timetable/class/${classId}`, { params });
  }

  getTeacherTimetable(teacherId: string, academicYearId: string): Observable<TimetableSlotDto[]> {
    const params = new HttpParams().set('academicYearId', academicYearId);
    return this.http.get<TimetableSlotDto[]>(`${this.base}/timetable/teacher/${teacherId}`, { params });
  }

  upsertSlot(req: UpsertSlotRequest): Observable<TimetableSlotDto> {
    return this.http.put<TimetableSlotDto>(`${this.base}/timetable/slot`, req);
  }

  deleteSlot(slotId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/timetable/slot/${slotId}`);
  }
}
