import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  StudentSummaryDto, StudentDto,
  CreateStudentRequest, UpdateStudentRequest,
  GuardianDto, CreateGuardianRequest,
  StudentFilter, PageResponse
} from '../models/student.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private base = `${environment.apiUrl}/students`;

  constructor(private http: HttpClient) {}

  // ── Student CRUD ────────────────────────────────────────────────────

  list(filter: StudentFilter): Observable<PageResponse<StudentSummaryDto>> {
    let p = new HttpParams();
    if (filter.search)   p = p.set('search',   filter.search);
    if (filter.classId)  p = p.set('classId',  filter.classId);
    if (filter.gender)   p = p.set('gender',   filter.gender);
    if (filter.category) p = p.set('category', filter.category);
    if (filter.isActive !== undefined) p = p.set('isActive', filter.isActive.toString());
    p = p.set('page', (filter.page ?? 0).toString());
    p = p.set('size', (filter.size ?? 20).toString());
    if (filter.sort) p = p.set('sort', filter.sort);
    return this.http.get<PageResponse<StudentSummaryDto>>(this.base, { params: p });
  }

  get(id: string): Observable<StudentDto> {
    return this.http.get<StudentDto>(`${this.base}/${id}`);
  }

  create(req: CreateStudentRequest): Observable<StudentDto> {
    return this.http.post<StudentDto>(this.base, req);
  }

  update(id: string, req: UpdateStudentRequest): Observable<StudentDto> {
    return this.http.put<StudentDto>(`${this.base}/${id}`, req);
  }

  setActive(id: string, active: boolean): Observable<void> {
    return this.http.patch<void>(
      `${this.base}/${id}/status`, null,
      { params: new HttpParams().set('active', active.toString()) }
    );
  }

  // ── Guardian sub-resource ────────────────────────────────────────────

  getGuardians(studentId: string): Observable<GuardianDto[]> {
    return this.http.get<GuardianDto[]>(`${this.base}/${studentId}/guardians`);
  }

  addGuardian(studentId: string, req: CreateGuardianRequest): Observable<GuardianDto> {
    return this.http.post<GuardianDto>(`${this.base}/${studentId}/guardians`, req);
  }

  updateGuardian(studentId: string, guardianId: string, req: CreateGuardianRequest): Observable<GuardianDto> {
    return this.http.put<GuardianDto>(`${this.base}/${studentId}/guardians/${guardianId}`, req);
  }

  deleteGuardian(studentId: string, guardianId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${studentId}/guardians/${guardianId}`);
  }
}
