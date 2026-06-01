import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import {
  LibraryService, BookDto, BookIssueDto,
  CreateBookRequest, IssueBookRequest,
} from '../../../core/services/library.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-library',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './library.component.html',
  styleUrls: ['./library.component.scss'],
})
export class LibraryComponent implements OnInit {
  private libSvc  = inject(LibraryService);
  private toast   = inject(ToastService);

  loading     = signal(true);
  search      = signal('');
  activeTab   = signal<'books' | 'issued'>('books');
  today       = new Date().toISOString().split('T')[0];

  books       = signal<BookDto[]>([]);
  issuedBooks = signal<BookIssueDto[]>([]);

  // Add Book modal
  showAddBook  = signal(false);
  savingBook   = signal(false);
  bookForm     = signal<CreateBookRequest>({ title: '', totalCopies: 1 });

  // Issue Book modal
  showIssue    = signal(false);
  issuingBook  = signal<BookDto | null>(null);
  savingIssue  = signal(false);
  issueForm    = signal<IssueBookRequest>({ bookId: '', borrowerName: '', issueDate: '', dueDate: '' });

  filteredBooks = computed(() => {
    const q = this.search().toLowerCase();
    return this.books().filter(b => !q || `${b.title} ${b.author ?? ''} ${b.isbn ?? ''}`.toLowerCase().includes(q));
  });

  overdueCount = computed(() => {
    return this.issuedBooks().filter(i => i.isOverdue).length;
  });

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.loading.set(true);
    this.libSvc.listBooks().subscribe({
      next: b => { this.books.set(b); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load books'); },
    });
    this.libSvc.listIssued().subscribe({
      next: i => this.issuedBooks.set(i),
      error: () => {},
    });
  }

  openAddBook(): void {
    this.bookForm.set({ title: '', author: '', isbn: '', category: '', totalCopies: 1 });
    this.showAddBook.set(true);
  }

  saveBook(): void {
    const f = this.bookForm();
    if (!f.title) { this.toast.warning('Book title is required'); return; }
    this.savingBook.set(true);
    this.libSvc.addBook(f).subscribe({
      next: () => {
        this.toast.success('Book added to catalogue');
        this.showAddBook.set(false);
        this.savingBook.set(false);
        this.loadAll();
      },
      error: () => { this.toast.error('Failed to add book'); this.savingBook.set(false); },
    });
  }

  deleteBook(id: string): void {
    if (!confirm('Remove this book from the catalogue?')) return;
    this.libSvc.deleteBook(id).subscribe({
      next: () => { this.toast.success('Book removed'); this.loadAll(); },
      error: () => this.toast.error('Failed to remove book'),
    });
  }

  openIssue(book: BookDto): void {
    this.issuingBook.set(book);
    const due = new Date(); due.setDate(due.getDate() + 14); // 2-week default
    this.issueForm.set({
      bookId: book.id, borrowerName: '',
      issueDate: this.today,
      dueDate: due.toISOString().split('T')[0],
    });
    this.showIssue.set(true);
  }

  saveIssue(): void {
    const f = this.issueForm();
    if (!f.borrowerName) { this.toast.warning('Borrower name is required'); return; }
    this.savingIssue.set(true);
    this.libSvc.issueBook(f).subscribe({
      next: () => {
        this.toast.success('Book issued successfully');
        this.showIssue.set(false);
        this.savingIssue.set(false);
        this.loadAll();
      },
      error: (err) => {
        this.toast.error(err?.error?.message ?? 'Failed to issue book');
        this.savingIssue.set(false);
      },
    });
  }

  returnBook(issueId: string): void {
    if (!confirm('Mark this book as returned?')) return;
    this.libSvc.returnBook(issueId).subscribe({
      next: () => { this.toast.success('Book returned'); this.loadAll(); },
      error: () => this.toast.error('Failed to process return'),
    });
  }

  updateBookForm(patch: Partial<CreateBookRequest>): void {
    this.bookForm.update(f => ({ ...f, ...patch }));
  }

  updateIssueForm(patch: Partial<IssueBookRequest>): void {
    this.issueForm.update(f => ({ ...f, ...patch }));
  }

  availabilityColor(b: BookDto) {
    if (b.availableCopies === 0) return 'red';
    if (b.availableCopies < 5)  return 'yellow';
    return 'green';
  }
}
