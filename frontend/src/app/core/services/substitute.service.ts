import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SubstituteAssignmentDto {
  id: string;
  absentTeacherId: string;
  absentTeacherName: string;
  substituteTeacherId: string | null;
  substituteTeacherName: string | null;
  absenceDate: string;
  periodNo: number;
  classId: string;
  className: string;
  subjectId: string | null;
  subjectName: string | null;
  startTime: string;
  endTime: string;
  status: 'PENDING' | 'SUGGESTED' | 'ASSIGNED' | 'SELF_STUDY' | 'CANCELLED';
  suggestionReason: string | null;
  confidenceScore: number | null;
  remarks: string | null;
}

export interface SubstituteSuggestionDto {
  teacherId: string;
  teacherName: string;
  department: string | null;
  reason: string;
  confidenceScore: number;
  teachesSameSubject: boolean;
  currentLoadToday: number;
}

@Injectable({ providedIn: 'root' })
export class SubstituteService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  reportAbsence(teacherId: string, absenceDate: string, remarks?: string): Observable<SubstituteAssignmentDto[]> {
    return this.http.post<SubstituteAssignmentDto[]>(`${this.base}/substitutes/report-absence`, {
      teacherId, absenceDate, remarks
    });
  }

  getByDate(date: string): Observable<SubstituteAssignmentDto[]> {
    return this.http.get<SubstituteAssignmentDto[]>(`${this.base}/substitutes/date/${date}`);
  }

  getPending(date: string): Observable<SubstituteAssignmentDto[]> {
    return this.http.get<SubstituteAssignmentDto[]>(`${this.base}/substitutes/pending/${date}`);
  }

  getSuggestions(assignmentId: string): Observable<SubstituteSuggestionDto[]> {
    return this.http.get<SubstituteSuggestionDto[]>(`${this.base}/substitutes/${assignmentId}/suggestions`);
  }

  assignSubstitute(assignmentId: string, substituteTeacherId: string | null, remarks?: string): Observable<SubstituteAssignmentDto> {
    return this.http.post<SubstituteAssignmentDto>(`${this.base}/substitutes/assign`, {
      assignmentId, substituteTeacherId, remarks
    });
  }
}
