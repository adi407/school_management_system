import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface SchoolRegistrationRequest {
  schoolName: string;
  schoolCode: string;
  board: string;
  requestedTier?: string;
  address?: string;
  city?: string;
  state?: string;
  phone?: string;
  schoolEmail?: string;
  website?: string;
  studentCount?: number;
  adminName: string;
  adminEmail: string;
  adminPhone?: string;
  adminDesignation?: string;
  message?: string;
}

export interface SchoolRegistrationDto {
  id: string;
  schoolName: string;
  schoolCode: string;
  board: string;
  requestedTier: string;
  address: string;
  city: string;
  state: string;
  phone: string;
  schoolEmail: string;
  website: string;
  studentCount: number;
  adminName: string;
  adminEmail: string;
  adminPhone: string;
  adminDesignation: string;
  message: string;
  status: 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';
  rejectionReason: string;
  approvedSchoolId: string;
  createdAt: string;
  reviewedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class RegistrationService {
  private publicBase = `${environment.apiUrl}/public`;
  private adminBase = `${environment.apiUrl}/super-admin`;

  constructor(private http: HttpClient) {}

  submitRegistration(req: SchoolRegistrationRequest) {
    return this.http.post<SchoolRegistrationDto>(`${this.publicBase}/register`, req);
  }

  listRegistrations(params: { status?: string; page?: number; size?: number }) {
    let p = new HttpParams();
    if (params.status) p = p.set('status', params.status);
    p = p.set('page', (params.page ?? 0).toString());
    p = p.set('size', (params.size ?? 20).toString());
    return this.http.get<PageResponse<SchoolRegistrationDto>>(`${this.adminBase}/registrations`, { params: p });
  }

  getRegistration(id: string) {
    return this.http.get<SchoolRegistrationDto>(`${this.adminBase}/registrations/${id}`);
  }

  getPendingCount() {
    return this.http.get<{ count: number }>(`${this.adminBase}/registrations/pending-count`);
  }

  approve(id: string, adminPassword: string, subscriptionTier?: string) {
    return this.http.post<SchoolRegistrationDto>(`${this.adminBase}/registrations/${id}/approve`, {
      adminPassword,
      subscriptionTier,
    });
  }

  reject(id: string, reason: string) {
    return this.http.post<SchoolRegistrationDto>(`${this.adminBase}/registrations/${id}/reject`, { reason });
  }
}
