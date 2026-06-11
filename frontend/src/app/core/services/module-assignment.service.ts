import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  AssignModuleRequest,
  MyModuleDto,
  SchoolUserDto,
  StaffModule,
  StaffModuleAssignmentDto,
} from '../models/module.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ModuleAssignmentService {
  private readonly base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** GET /api/v1/my-modules */
  getMyModules() {
    return this.http.get<MyModuleDto[]>(`${this.base}/my-modules`);
  }

  /** GET /api/v1/modules/school */
  getSchoolAssignments() {
    return this.http.get<StaffModuleAssignmentDto[]>(`${this.base}/modules/school`);
  }

  /** GET /api/v1/staff/:id/modules */
  getStaffModules(staffId: string) {
    return this.http.get<StaffModuleAssignmentDto[]>(`${this.base}/staff/${staffId}/modules`);
  }

  /** POST /api/v1/staff/:id/modules */
  assignModule(staffId: string, req: AssignModuleRequest) {
    return this.http.post<StaffModuleAssignmentDto>(
      `${this.base}/staff/${staffId}/modules`,
      req
    );
  }

  /** DELETE /api/v1/staff/:id/modules/:module */
  revokeModule(staffId: string, module: StaffModule) {
    return this.http.delete<void>(`${this.base}/staff/${staffId}/modules/${module}`);
  }

  // ── Super-admin endpoints ─────────────────────────────────────────────────

  private sa(schoolId: string) {
    return `${this.base}/super-admin/schools/${schoolId}/staff`;
  }

  /** GET /api/v1/super-admin/schools/:schoolId/staff */
  getSchoolUsers(schoolId: string) {
    return this.http.get<SchoolUserDto[]>(this.sa(schoolId));
  }

  /** GET /api/v1/super-admin/schools/:schoolId/staff/:userId/modules */
  getSaUserModules(schoolId: string, userId: string) {
    return this.http.get<StaffModuleAssignmentDto[]>(`${this.sa(schoolId)}/${userId}/modules`);
  }

  /** POST /api/v1/super-admin/schools/:schoolId/staff/:userId/modules */
  saAssignModule(schoolId: string, userId: string, req: AssignModuleRequest) {
    return this.http.post<StaffModuleAssignmentDto>(
      `${this.sa(schoolId)}/${userId}/modules`, req);
  }

  /** DELETE /api/v1/super-admin/schools/:schoolId/staff/:userId/modules/:module */
  saRevokeModule(schoolId: string, userId: string, module: StaffModule) {
    return this.http.delete<void>(`${this.sa(schoolId)}/${userId}/modules/${module}`);
  }

  /** POST /api/v1/super-admin/schools/:schoolId/staff/:userId/modules/grant-all */
  saGrantAllModules(schoolId: string, userId: string) {
    return this.http.post<void>(`${this.sa(schoolId)}/${userId}/modules/grant-all`, {});
  }

  /** POST /api/v1/super-admin/schools/:schoolId/staff/:userId/reset-password */
  saResetPassword(schoolId: string, userId: string, newPassword: string) {
    return this.http.post<void>(
      `${this.sa(schoolId)}/${userId}/reset-password`,
      { newPassword }
    );
  }
}
