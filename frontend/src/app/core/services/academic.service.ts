import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AcademicYearDto, ClassDto, SubjectDto, CreateClassRequest, CreateSubjectRequest, ClassSubjectDto, AssignSubjectRequest, CreateAcademicYearRequest } from '../models/academic.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AcademicService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ── Academic Years ─────────────────────────────────────────────────────────
  listYears(): Observable<AcademicYearDto[]> {
    return this.http.get<AcademicYearDto[]>(`${this.base}/academic-years`);
  }

  createYear(req: CreateAcademicYearRequest): Observable<AcademicYearDto> {
    return this.http.post<AcademicYearDto>(`${this.base}/academic-years`, req);
  }

  updateYear(id: string, req: CreateAcademicYearRequest): Observable<AcademicYearDto> {
    return this.http.put<AcademicYearDto>(`${this.base}/academic-years/${id}`, req);
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

  // ── Class Subjects ─────────────────────────────────────────────────────────
  listClassSubjects(classId: string): Observable<ClassSubjectDto[]> {
    return this.http.get<ClassSubjectDto[]>(`${this.base}/classes/${classId}/subjects`);
  }

  assignSubject(classId: string, req: AssignSubjectRequest): Observable<ClassSubjectDto> {
    return this.http.post<ClassSubjectDto>(`${this.base}/classes/${classId}/subjects`, req);
  }

  updateSubjectTeacher(classId: string, cstId: string, teacherId: string | null): Observable<ClassSubjectDto> {
    return this.http.patch<ClassSubjectDto>(
      `${this.base}/classes/${classId}/subjects/${cstId}/teacher`,
      { teacherId }
    );
  }

  removeSubject(classId: string, cstId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/classes/${classId}/subjects/${cstId}`);
  }
}
