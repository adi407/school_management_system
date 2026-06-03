import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  AssignModuleRequest,
  MyModuleDto,
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
}
