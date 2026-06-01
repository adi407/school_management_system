import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface BookDto {
  id: string; title: string; author: string | null; isbn: string | null;
  category: string | null; totalCopies: number; availableCopies: number;
}
export interface BookIssueDto {
  id: string; bookId: string; bookTitle: string; studentId: string | null;
  borrowerName: string | null; issueDate: string; dueDate: string;
  returnDate: string | null; isReturned: boolean; isOverdue: boolean;
}
export interface CreateBookRequest { title: string; author?: string; isbn?: string; category?: string; totalCopies: number; }
export interface IssueBookRequest  { bookId: string; studentId?: string; borrowerName?: string; issueDate: string; dueDate: string; }

@Injectable({ providedIn: 'root' })
export class LibraryService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/library`;

  listBooks(): Observable<BookDto[]>                              { return this.http.get<BookDto[]>(`${this.base}/books`); }
  addBook(req: CreateBookRequest): Observable<BookDto>            { return this.http.post<BookDto>(`${this.base}/books`, req); }
  deleteBook(id: string): Observable<void>                        { return this.http.delete<void>(`${this.base}/books/${id}`); }
  listIssued(): Observable<BookIssueDto[]>                        { return this.http.get<BookIssueDto[]>(`${this.base}/issued`); }
  issueBook(req: IssueBookRequest): Observable<BookIssueDto>      { return this.http.post<BookIssueDto>(`${this.base}/issue`, req); }
  returnBook(issueId: string): Observable<BookIssueDto>           { return this.http.post<BookIssueDto>(`${this.base}/return/${issueId}`, {}); }
}
