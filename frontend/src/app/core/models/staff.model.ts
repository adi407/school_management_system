export interface StaffDto {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  fullName: string;
  phone: string | null;
  department: string | null;
  role: string;
  isActive: boolean;
  createdAt: string;
}

export interface CreateStaffRequest {
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  department: string | null;
  role: string;
  password: string | null;
}

export interface UpdateStaffRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  department?: string;
  role?: string;
  isActive?: boolean;
}

export const STAFF_ROLES = ['TEACHER', 'ACCOUNTANT', 'LIBRARIAN', 'TRANSPORT_MANAGER', 'HOSTEL_WARDEN'] as const;
