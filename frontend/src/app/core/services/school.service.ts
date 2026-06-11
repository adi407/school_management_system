import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CreateSchoolRequest, PageResponse, SchoolDto } from '../models/school.model';
import { FeatureFlagsMap } from '../models/feature-flag.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SchoolService {
  private base = `${environment.apiUrl}/super-admin`;

  constructor(private http: HttpClient) {}

  listSchools(params: { search?: string; tier?: string; isActive?: boolean; page?: number; size?: number }) {
    let p = new HttpParams();
    if (params.search)   p = p.set('search', params.search);
    if (params.tier)     p = p.set('tier', params.tier);
    if (params.isActive !== undefined) p = p.set('isActive', params.isActive.toString());
    p = p.set('page', (params.page ?? 0).toString());
    p = p.set('size', (params.size ?? 20).toString());
    p = p.set('sort', 'name');
    return this.http.get<PageResponse<SchoolDto>>(`${this.base}/schools`, { params: p });
  }

  getSchool(id: string) {
    return this.http.get<SchoolDto>(`${this.base}/schools/${id}`);
  }

  createSchool(req: CreateSchoolRequest) {
    return this.http.post<SchoolDto>(`${this.base}/schools`, req);
  }

  updateSchool(id: string, req: Partial<CreateSchoolRequest>) {
    return this.http.put<SchoolDto>(`${this.base}/schools/${id}`, req);
  }

  setStatus(id: string, active: boolean) {
    return this.http.patch<void>(`${this.base}/schools/${id}/status`, null, {
      params: new HttpParams().set('active', active.toString())
    });
  }

  getFeatures(id: string) {
    return this.http.get<FeatureFlagsMap>(`${this.base}/schools/${id}/features`);
  }

  updateFeatures(id: string, flags: Record<string, boolean>) {
    return this.http.put<void>(`${this.base}/schools/${id}/features`, { flags });
  }

  softDelete(id: string) {
    return this.http.patch<DeleteSchoolResponse>(`${this.base}/schools/${id}/soft-delete`, {});
  }

  hardDelete(id: string) {
    return this.http.delete<DeleteSchoolResponse>(`${this.base}/schools/${id}`);
  }
}

export interface DeleteSchoolResponse {
  schoolName: string;
  deleteType: string;
  usersAffected: number;
  studentsAffected: number;
  totalRecordsDeleted: number;
}
