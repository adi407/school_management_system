import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PtmMeetingDto {
  id: string;
  title: string;
  classId: string | null;
  className: string | null;
  academicYearId: string;
  meetingDate: string;
  startTime: string | null;
  endTime: string | null;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  notes: string | null;
  createdByName: string | null;
  briefingCount: number;
}

export interface PtmBriefingDto {
  id: string;
  ptmMeetingId: string;
  studentId: string;
  studentName: string;
  className: string | null;
  teacherId: string | null;
  teacherName: string | null;
  attendancePct: number | null;
  avgMarks: number | null;
  homeworkCompletionPct: number | null;
  wellnessTrend: 'IMPROVING' | 'STABLE' | 'DECLINING' | null;
  aiSummary: string | null;
  talkingPoints: string | null;
  parentPreview: string | null;
  status: 'DRAFT' | 'REVIEWED' | 'SENT_TO_PARENT';
}

@Injectable({ providedIn: 'root' })
export class PtmService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createMeeting(req: { title: string; classId?: string; academicYearId: string; meetingDate: string; startTime?: string; endTime?: string; notes?: string }): Observable<PtmMeetingDto> {
    return this.http.post<PtmMeetingDto>(`${this.base}/ptm/meetings`, req);
  }

  listMeetings(): Observable<PtmMeetingDto[]> {
    return this.http.get<PtmMeetingDto[]>(`${this.base}/ptm/meetings`);
  }

  getMeeting(meetingId: string): Observable<PtmMeetingDto> {
    return this.http.get<PtmMeetingDto>(`${this.base}/ptm/meetings/${meetingId}`);
  }

  generateBriefings(meetingId: string): Observable<PtmBriefingDto[]> {
    return this.http.post<PtmBriefingDto[]>(`${this.base}/ptm/meetings/${meetingId}/generate-briefings`, {});
  }

  getBriefings(meetingId: string): Observable<PtmBriefingDto[]> {
    return this.http.get<PtmBriefingDto[]>(`${this.base}/ptm/meetings/${meetingId}/briefings`);
  }

  reviewBriefing(briefingId: string): Observable<PtmBriefingDto> {
    return this.http.post<PtmBriefingDto>(`${this.base}/ptm/briefings/${briefingId}/review`, {});
  }
}
