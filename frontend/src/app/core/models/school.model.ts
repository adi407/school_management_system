export type BoardType = 'CBSE' | 'ICSE' | 'STATE' | 'IB';
export type SubscriptionTier = 'FREE' | 'BASIC' | 'PREMIUM' | 'ENTERPRISE';

export interface SchoolDto {
  id: string;
  name: string;
  code: string;
  board: BoardType;
  subscriptionTier: SubscriptionTier;
  subscriptionExpiry: string | null;
  address: string | null;
  phone: string | null;
  email: string | null;
  logoUrl: string | null;
  timezone: string;
  locale: string;
  isActive: boolean;
  studentCount: number;
  staffCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSchoolRequest {
  name: string;
  code: string;
  board: BoardType;
  subscriptionTier: SubscriptionTier;
  address?: string;
  phone?: string;
  email?: string;
  timezone?: string;
  subscriptionExpiry?: string;
  adminEmail: string;
  adminPassword: string;
  adminName?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
