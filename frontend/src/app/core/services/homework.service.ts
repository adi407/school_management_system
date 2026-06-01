import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HomeworkDto, CreateHomeworkRequest, HomeworkFilter, PageResponse } from '../models/homework.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class HomeworkService {
  private base = `${environment.apiUrl}/homework`;

  constructor(private http: HttpClient) {}

  // ── Teacher / Admin: paginated list ───────────────────────────────────────
  list(filter: HomeworkFilter): Observable<PageResponse<HomeworkDto>> {
    let p = new HttpParams();
    if (filter.classId)   p = p.set('classId',   filter.classId);
    if (filter.subjectId) p = p.set('subjectId', filter.subjectId);
    if (filter.from)      p = p.set('from',       filter.from);
    if (filter.to)        p = p.set('to',         filter.to);
    p = p.set('page', (filter.page ?? 0).toString());
    p = p.set('size', (filter.size ?? 20).toString());
    p = p.set('sort', 'dueDate,asc');
    return this.http.get<PageResponse<HomeworkDto>>(this.base, { params: p });
  }

  get(id: string): Observable<HomeworkDto> {
    return this.http.get<HomeworkDto>(`${this.base}/${id}`);
  }

  create(req: CreateHomeworkRequest): Observable<HomeworkDto> {
    return this.http.post<HomeworkDto>(this.base, req);
  }

  update(id: string, req: CreateHomeworkRequest): Observable<HomeworkDto> {
    return this.http.put<HomeworkDto>(`${this.base}/${id}`, req);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  setPublished(id: string, published: boolean): Observable<HomeworkDto> {
    return this.http.patch<HomeworkDto>(
      `${this.base}/${id}/publish`,
      null,
      { params: new HttpParams().set('published', published.toString()) }
    );
  }

  // ── Parent / Student: upcoming homework for a class ───────────────────────
  getUpcoming(classId: string): Observable<HomeworkDto[]> {
    return this.http.get<HomeworkDto[]>(`${this.base}/upcoming`, {
      params: new HttpParams().set('classId', classId)
    });
  }

  // ── Parent: homework in a date range ─────────────────────────────────────
  getForClass(classId: string, from: string, to: string): Observable<HomeworkDto[]> {
    return this.http.get<HomeworkDto[]>(`${this.base}/class/${classId}`, {
      params: new HttpParams().set('from', from).set('to', to)
    });
  }
}
