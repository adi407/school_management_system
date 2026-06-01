export interface FeeStructureDto {
  id: string;
  classId: string | null;
  className: string | null;
  academicYearId: string | null;
  academicYearName: string | null;
  feeType: string;
  amount: number;
  dueDate: string | null;
  isRecurring: boolean;
  frequency: string | null;
  description: string | null;
}

export interface FeePaymentDto {
  id: string;
  studentId: string;
  studentName: string;
  admissionNo: string;
  feeStructureId: string | null;
  feeType: string | null;
  amountPaid: number;
  paymentDate: string;
  paymentMode: string;
  receiptNo: string;
  remarks: string | null;
  collectedById: string;
  collectedByEmail: string;
}

export interface StudentFeesSummaryDto {
  studentId: string;
  studentName: string;
  totalFees: number;
  totalPaid: number;
  balance: number;
  structures: FeeStructureDto[];
  payments: FeePaymentDto[];
}

export interface CreateFeeStructureRequest {
  classId: string | null;
  academicYearId: string | null;
  feeType: string;
  amount: number;
  dueDate: string | null;
  isRecurring: boolean;
  frequency: string | null;
  description: string | null;
}

export interface RecordPaymentRequest {
  studentId: string;
  feeStructureId: string | null;
  amountPaid: number;
  paymentDate: string;
  paymentMode: string;
  remarks: string | null;
}
