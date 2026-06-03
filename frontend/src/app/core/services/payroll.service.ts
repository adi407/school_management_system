import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  SalaryStructureDto, CreateSalaryStructureRequest,
  PayrollRunDto, TriggerPayrollRequest,
  PayslipDto,
  StaffAttendanceDto, MarkStaffAttendanceRequest,
  ExpenseEntryDto, CreateExpenseRequest,
  ProfitLossReportDto,
} from '../models/payroll.model';

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ── Salary Structures ─────────────────────────────────────────────────────

  listStructures(): Observable<SalaryStructureDto[]> {
    return this.http.get<SalaryStructureDto[]>(`${this.base}/salary-structures`);
  }

  listStructuresForStaff(staffId: string): Observable<SalaryStructureDto[]> {
    return this.http.get<SalaryStructureDto[]>(`${this.base}/salary-structures/staff/${staffId}`);
  }

  createStructure(req: CreateSalaryStructureRequest): Observable<SalaryStructureDto> {
    return this.http.post<SalaryStructureDto>(`${this.base}/salary-structures`, req);
  }

  updateStructure(id: string, req: CreateSalaryStructureRequest): Observable<SalaryStructureDto> {
    return this.http.put<SalaryStructureDto>(`${this.base}/salary-structures/${id}`, req);
  }

  updateDeclarations(id: string, declared80c?: number, declaredHra?: number, declaredOther?: number): Observable<SalaryStructureDto> {
    let params = new HttpParams();
    if (declared80c  != null) params = params.set('declared80c',  declared80c);
    if (declaredHra  != null) params = params.set('declaredHra',  declaredHra);
    if (declaredOther != null) params = params.set('declaredOther', declaredOther);
    return this.http.patch<SalaryStructureDto>(`${this.base}/salary-structures/${id}/declarations`, null, { params });
  }

  // ── Payroll Runs ──────────────────────────────────────────────────────────

  listRuns(): Observable<PayrollRunDto[]> {
    return this.http.get<PayrollRunDto[]>(`${this.base}/payroll/runs`);
  }

  triggerRun(req: TriggerPayrollRequest): Observable<PayrollRunDto> {
    return this.http.post<PayrollRunDto>(`${this.base}/payroll/runs`, req);
  }

  approveRun(runId: string): Observable<PayrollRunDto> {
    return this.http.post<PayrollRunDto>(`${this.base}/payroll/runs/${runId}/approve`, null);
  }

  markPaid(runId: string): Observable<PayrollRunDto> {
    return this.http.post<PayrollRunDto>(`${this.base}/payroll/runs/${runId}/mark-paid`, null);
  }

  // ── Payslips ─────────────────────────────────────────────────────────────

  getPayslipsForRun(runId: string): Observable<PayslipDto[]> {
    return this.http.get<PayslipDto[]>(`${this.base}/payroll/runs/${runId}/payslips`);
  }

  getMyPayslips(): Observable<PayslipDto[]> {
    return this.http.get<PayslipDto[]>(`${this.base}/payroll/my-payslips`);
  }

  // ── Staff Attendance ──────────────────────────────────────────────────────

  markStaffAttendance(req: MarkStaffAttendanceRequest): Observable<StaffAttendanceDto[]> {
    return this.http.post<StaffAttendanceDto[]>(`${this.base}/staff-attendance/mark`, req);
  }

  getDayRoll(date: string): Observable<StaffAttendanceDto[]> {
    return this.http.get<StaffAttendanceDto[]>(`${this.base}/staff-attendance/day`, {
      params: new HttpParams().set('date', date),
    });
  }

  // ── Expenses ──────────────────────────────────────────────────────────────

  listExpenses(from: string, to: string): Observable<ExpenseEntryDto[]> {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<ExpenseEntryDto[]>(`${this.base}/expenses`, { params });
  }

  createExpense(req: CreateExpenseRequest): Observable<ExpenseEntryDto> {
    return this.http.post<ExpenseEntryDto>(`${this.base}/expenses`, req);
  }

  updateExpense(id: string, req: CreateExpenseRequest): Observable<ExpenseEntryDto> {
    return this.http.put<ExpenseEntryDto>(`${this.base}/expenses/${id}`, req);
  }

  deleteExpense(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/expenses/${id}`);
  }

  // ── P&L Report ────────────────────────────────────────────────────────────

  getProfitLoss(year: number, fromMonth: number, toMonth: number): Observable<ProfitLossReportDto> {
    const params = new HttpParams()
      .set('year', year)
      .set('fromMonth', fromMonth)
      .set('toMonth', toMonth);
    return this.http.get<ProfitLossReportDto>(`${this.base}/finance/reports/pl`, { params });
  }
}
