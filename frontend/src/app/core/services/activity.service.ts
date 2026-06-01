import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ActivityDto { id: string; name: string; category: string; coach: string | null; schedule: string | null; capacity: number; status: string; }
export interface CreateActivityRequest { name: string; category: string; coach?: string; schedule?: string; capacity: number; status?: string; }

@Injectable({ providedIn: 'root' })
export class ActivityService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/activities`;
  list(): Observable<ActivityDto[]>                                    { return this.http.get<ActivityDto[]>(this.base); }
  create(req: CreateActivityRequest): Observable<ActivityDto>          { return this.http.post<ActivityDto>(this.base, req); }
  update(id: string, req: CreateActivityRequest): Observable<ActivityDto> { return this.http.put<ActivityDto>(`${this.base}/${id}`, req); }
  delete(id: string): Observable<void>                                 { return this.http.delete<void>(`${this.base}/${id}`); }
}
