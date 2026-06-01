import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AnnouncementDto, CreateAnnouncementRequest } from '../models/announcement.model';

@Injectable({ providedIn: 'root' })
export class AnnouncementService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  list(): Observable<AnnouncementDto[]> {
    return this.http.get<AnnouncementDto[]>(`${this.base}/announcements`);
  }

  listAll(): Observable<AnnouncementDto[]> {
    return this.http.get<AnnouncementDto[]>(`${this.base}/announcements/all`);
  }

  create(req: CreateAnnouncementRequest): Observable<AnnouncementDto> {
    return this.http.post<AnnouncementDto>(`${this.base}/announcements`, req);
  }

  update(id: string, req: CreateAnnouncementRequest): Observable<AnnouncementDto> {
    return this.http.put<AnnouncementDto>(`${this.base}/announcements/${id}`, req);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/announcements/${id}`);
  }
}
