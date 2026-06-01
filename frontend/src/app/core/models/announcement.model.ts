export interface AnnouncementDto {
  id: string;
  title: string;
  body: string;
  targetRoles: string[];
  publishedById: string;
  publishedByName: string;
  publishedAt: string;
  expiresAt: string | null;
  isPinned: boolean;
}

export interface CreateAnnouncementRequest {
  title: string;
  body: string;
  targetRoles: string[];
  expiresAt: string | null;
  isPinned: boolean;
}
