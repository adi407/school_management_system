import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AcademicYearDto, ClassDto, SubjectDto, CreateClassRequest, CreateSubjectRequest } from '../models/academic.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AcademicService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ── Academic Years ─────────────────────────────────────────────────────────
  listYears(): Observable<AcademicYearDto[]> {
    return this.http.get<AcademicYearDto[]>(`${this.base}/academic-years`);
  }

  // ── Classes ────────────────────────────────────────────────────────────────
  listClasses(): Observable<ClassDto[]> {
    return this.http.get<ClassDto[]>(`${this.base}/classes`);
  }

  getClass(id: string): Observable<ClassDto> {
    return this.http.get<ClassDto>(`${this.base}/classes/${id}`);
  }

  createClass(req: CreateClassRequest): Observable<ClassDto> {
    return this.http.post<ClassDto>(`${this.base}/classes`, req);
  }

  updateClass(id: string, req: Partial<CreateClassRequest>): Observable<ClassDto> {
    return this.http.put<ClassDto>(`${this.base}/classes/${id}`, req);
  }

  // ── Subjects ───────────────────────────────────────────────────────────────
  listSubjects(): Observable<SubjectDto[]> {
    return this.http.get<SubjectDto[]>(`${this.base}/subjects`);
  }

  createSubject(req: CreateSubjectRequest): Observable<SubjectDto> {
    return this.http.post<SubjectDto>(`${this.base}/subjects`, req);
  }

  updateSubject(id: string, req: Partial<CreateSubjectRequest>): Observable<SubjectDto> {
    return this.http.put<SubjectDto>(`${this.base}/subjects/${id}`, req);
  }

  deleteSubject(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/subjects/${id}`);
  }
}
