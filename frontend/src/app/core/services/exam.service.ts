import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ExamDto {
  id: string;
  academicYearId: string | null;
  academicYearName: string | null;
  classId: string | null;
  className: string;
  name: string;
  examType: string;
  startDate: string;
  endDate: string;
  totalSubjects: number;
  description: string | null;
  status: string;
}

export interface CreateExamRequest {
  academicYearId?: string;
  classId?: string;
  name: string;
  examType: string;
  startDate: string;
  endDate: string;
  totalSubjects: number;
  description?: string;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class ExamService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/exams`;

  list(): Observable<ExamDto[]>                              { return this.http.get<ExamDto[]>(this.base); }
  create(req: CreateExamRequest): Observable<ExamDto>        { return this.http.post<ExamDto>(this.base, req); }
  update(id: string, req: CreateExamRequest): Observable<ExamDto> { return this.http.put<ExamDto>(`${this.base}/${id}`, req); }
  delete(id: string): Observable<void>                       { return this.http.delete<void>(`${this.base}/${id}`); }
}
