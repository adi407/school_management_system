import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StaffDto, CreateStaffRequest, UpdateStaffRequest } from '../models/staff.model';

@Injectable({ providedIn: 'root' })
export class StaffService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  list(): Observable<StaffDto[]> {
    return this.http.get<StaffDto[]>(`${this.base}/staff`);
  }

  create(req: CreateStaffRequest): Observable<StaffDto> {
    return this.http.post<StaffDto>(`${this.base}/staff`, req);
  }

  update(id: string, req: UpdateStaffRequest): Observable<StaffDto> {
    return this.http.patch<StaffDto>(`${this.base}/staff/${id}`, req);
  }

  deactivate(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/staff/${id}`);
  }
}
