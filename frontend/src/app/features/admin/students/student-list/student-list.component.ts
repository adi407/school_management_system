import { Component, signal, computed, inject, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { StudentSummaryDto, Gender, StudentCategory, PageResponse } from '../../../../core/models/student.model';
import { StudentService } from '../../../../core/services/student.service';

@Component({
  selector: 'sms-student-list',
  standalone: true,
  imports: [RouterModule, FormsModule, DatePipe],
  templateUrl: './student-list.component.html',
  styleUrls: ['./student-list.component.scss'],
})
export class StudentListComponent implements OnInit, OnDestroy {
  private studentService = inject(StudentService);
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  // ── Filter state ─────────────────────────────────────────────────
  search         = signal('');
  selectedClass  = signal('');
  selectedGender = signal<Gender | ''>('');
  selectedStatus = signal<'active' | 'inactive' | ''>('');
  currentPage    = signal(0);
  readonly pageSize = 20;

  // ── Data state (server-driven) ────────────────────────────────────
  loading      = signal(false);
  page         = signal<PageResponse<StudentSummaryDto>>({
    content: [], totalElements: 0, totalPages: 0, number: 0, size: 20
  });

  // ── Computed convenience ──────────────────────────────────────────
  students   = computed(() => this.page().content);
  totalPages = computed(() => this.page().totalPages);
  totalElements = computed(() => this.page().totalElements);
  displayEnd = computed(() =>
    Math.min((this.currentPage() + 1) * this.pageSize, this.totalElements())
  );

  // Mock classes until SchoolClass API is wired
  classes: { id: string; name: string }[] = [];

  ngOnInit() {
    // Debounce search input
    this.searchSubject.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage.set(0);
      this.load();
    });

    this.load();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load() {
    this.loading.set(true);
    const status = this.selectedStatus();
    this.studentService.list({
      search:   this.search() || undefined,
      classId:  this.selectedClass() || undefined,
      gender:   (this.selectedGender() || undefined) as Gender | undefined,
      isActive: status === 'active' ? true : status === 'inactive' ? false : undefined,
      page:     this.currentPage(),
      size:     this.pageSize,
      sort:     'lastName',
    }).subscribe({
      next: res => { this.page.set(res); this.loading.set(false); },
      error: ()  => this.loading.set(false),
    });
  }

  onSearch(val: string) {
    this.search.set(val);
    this.searchSubject.next(val);
  }

  onClassFilter(val: string)  { this.selectedClass.set(val);  this.currentPage.set(0); this.load(); }
  onGenderFilter(val: string) { this.selectedGender.set(val as Gender | ''); this.currentPage.set(0); this.load(); }
  onStatusFilter(val: string) { this.selectedStatus.set(val as 'active' | 'inactive' | ''); this.currentPage.set(0); this.load(); }

  prevPage() { if (this.currentPage() > 0) { this.currentPage.update(p => p - 1); this.load(); } }
  nextPage() { if (this.currentPage() < this.totalPages() - 1) { this.currentPage.update(p => p + 1); this.load(); } }

  initials(s: StudentSummaryDto) {
    return ((s.firstName?.[0] ?? '') + (s.lastName?.[0] ?? '')).toUpperCase();
  }

  genderColor(g: Gender) {
    return g === 'MALE' ? 'blue' : g === 'FEMALE' ? 'purple' : 'gray';
  }

  categoryColor(c: StudentCategory) {
    const map: Record<StudentCategory, string> = {
      GEN: 'gray', OBC: 'blue', SC: 'orange', ST: 'green', EWS: 'yellow'
    };
    return map[c] ?? 'gray';
  }
}
