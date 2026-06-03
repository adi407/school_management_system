// ── Enums ────────────────────────────────────────────────────────────────────

export type PayrollStatus  = 'DRAFT' | 'APPROVED' | 'PAID';
export type TaxRegime      = 'OLD' | 'NEW';
export type ExpenseCategory =
  | 'RENT' | 'ELECTRICITY' | 'WATER' | 'INTERNET' | 'MAINTENANCE'
  | 'STATIONARY' | 'CLEANING' | 'SECURITY' | 'TRANSPORT'
  | 'MARKETING' | 'EQUIPMENT' | 'SOFTWARE' | 'MISCELLANEOUS';

// ── Salary Structure ──────────────────────────────────────────────────────────

export interface SalaryStructureDto {
  id: string;
  staffId: string;
  staffName: string;
  staffEmail: string;
  staffRole: string;
  basicSalary: number;
  hraAmount: number;
  daAmount: number;
  taAmount: number;
  medicalAllowance: number;
  otherAllowances: number;
  grossSalary: number;
  pfEnrolled: boolean;
  pfWageCeiling: number;
  taxRegime: TaxRegime;
  declared80c: number;
  declaredHraExemption: number;
  declaredOtherExemptions: number;
  effectiveFrom: string;
  effectiveTo: string | null;
  isActive: boolean;
}

export interface CreateSalaryStructureRequest {
  staffId: string;
  basicSalary: number;
  hraAmount: number;
  daAmount: number;
  taAmount: number;
  medicalAllowance: number;
  otherAllowances: number;
  pfEnrolled: boolean;
  pfWageCeiling: number | null;
  taxRegime: TaxRegime;
  declared80c: number;
  declaredHraExemption: number;
  declaredOtherExemptions: number;
  effectiveFrom: string;
  effectiveTo: string | null;
}

// ── Payroll Run ───────────────────────────────────────────────────────────────

export interface PayrollRunDto {
  id: string;
  runMonth: number;
  runYear: number;
  totalWorkingDays: number;
  status: PayrollStatus;
  totalGross: number;
  totalPfEmployee: number;
  totalPfEmployer: number;
  totalEsiEmployee: number;
  totalEsiEmployer: number;
  totalProfessionalTax: number;
  totalTds: number;
  totalLopDeduction: number;
  totalNetPayout: number;
  triggeredByName: string | null;
  approvedByName: string | null;
  approvedAt: string | null;
  paidAt: string | null;
  notes: string | null;
  payslipCount: number;
}

export interface TriggerPayrollRequest {
  month: number;
  year: number;
  totalWorkingDays: number | null;
  notes: string | null;
}

// ── Payslip ───────────────────────────────────────────────────────────────────

export interface PayslipDto {
  id: string;
  payrollRunId: string;
  runMonth: number;
  runYear: number;
  staffId: string;
  staffName: string;
  staffEmail: string;
  staffRole: string;
  department: string | null;
  basicSalary: number;
  hraAmount: number;
  daAmount: number;
  taAmount: number;
  medicalAllowance: number;
  otherAllowances: number;
  grossSalary: number;
  totalWorkingDays: number;
  presentDays: number;
  lopDays: number;
  lopDeduction: number;
  effectiveGross: number;
  pfEmployee: number;
  pfEmployer: number;
  esiEmployee: number;
  esiEmployer: number;
  professionalTax: number;
  tds: number;
  totalDeductions: number;
  netSalary: number;
}

// ── Staff Attendance ──────────────────────────────────────────────────────────

export interface StaffAttendanceDto {
  id: string;
  staffId: string;
  staffName: string;
  staffEmail: string;
  attendanceDate: string;
  status: string;
  remarks: string | null;
}

export interface MarkStaffAttendanceRequest {
  date: string;
  entries: { staffId: string; status: string; remarks: string | null }[];
}

// ── Expenses ─────────────────────────────────────────────────────────────────

export interface ExpenseEntryDto {
  id: string;
  category: ExpenseCategory;
  description: string;
  amount: number;
  expenseDate: string;
  referenceNo: string | null;
  attachmentUrl: string | null;
  enteredByName: string | null;
}

export interface CreateExpenseRequest {
  category: ExpenseCategory;
  description: string;
  amount: number;
  expenseDate: string;
  referenceNo: string | null;
  attachmentUrl: string | null;
}

// ── P&L Report ───────────────────────────────────────────────────────────────

export interface ProfitLossReportDto {
  fromMonth: number;
  toMonth: number;
  year: number;
  totalFeeCollected: number;
  otherIncome: number;
  totalRevenue: number;
  totalSalaryPayout: number;
  totalEmployerPf: number;
  totalEmployerEsi: number;
  totalPayrollCost: number;
  totalOperationalExpenses: number;
  expenseBreakdown: { category: string; amount: number }[];
  totalExpenses: number;
  netProfit: number;
  netProfitMarginPct: number;
}
