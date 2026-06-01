import { Component, signal, computed, inject, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe, NgClass } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { HomeworkService } from '../../../../core/services/homework.service';
import { AcademicService } from '../../../../core/services/academic.service';
import { ToastService } from '../../../../core/services/toast.service';
import { HomeworkDto } from '../../../../core/models/homework.model';
import { ClassDto, SubjectDto } from '../../../../core/models/academic.model';

@Component({
  selector: 'sms-homework-list',
  standalone: true,
  imports: [RouterModule, FormsModule, DatePipe, NgClass],
  templateUrl: './homework-list.component.html',
  styleUrls: ['./homework-list.component.scss'],
})
export class HomeworkListComponent implements OnInit, OnDestroy {
  private homeworkService = inject(HomeworkService);
  private academicService = inject(AcademicService);
  private toast           = inject(ToastService);
  private destroy$        = new Subject<void>();

  // Filter state
  selectedClassId   = '';
  selectedSubjectId = '';

  // Data
  homework   = signal<HomeworkDto[]>([]);
  classes    = signal<ClassDto[]>([]);
  subjects   = signal<SubjectDto[]>([]);
  loading    = signal(false);
  totalElements = signal(0);
  page       = signal(0);

  // Computed helpers
  totalPages = computed(() => Math.ceil(this.totalElements() / 20));
  displayEnd = computed(() => Math.min((this.page() + 1) * 20, this.totalElements()));

  ngOnInit() {
    // Load filter options
    this.academicService.listClasses().pipe(takeUntil(this.destroy$)).subscribe(cls => {
      this.classes.set(cls);
    });
    this.academicService.listSubjects().pipe(takeUntil(this.destroy$)).subscribe(subs => {
      this.subjects.set(subs);
    });
    this.load();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load() {
    this.loading.set(true);
    this.homeworkService.list({
      classId:   this.selectedClassId   || undefined,
      subjectId: this.selectedSubjectId || undefined,
      page:      this.page(),
      size:      20,
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.homework.set(res.content);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.toast.error('Failed to load homework');
        this.loading.set(false);
      },
    });
  }

  applyFilters() {
    this.page.set(0);
    this.load();
  }

  clearFilters() {
    this.selectedClassId   = '';
    this.selectedSubjectId = '';
    this.applyFilters();
  }

  prevPage() { if (this.page() > 0) { this.page.update(p => p - 1); this.load(); } }
  nextPage() { if (this.page() < this.totalPages() - 1) { this.page.update(p => p + 1); this.load(); } }

  togglePublish(hw: HomeworkDto) {
    this.homeworkService.setPublished(hw.id, !hw.isPublished).subscribe({
      next: updated => {
        this.homework.update(list => list.map(h => h.id === updated.id ? updated : h));
        this.toast.success(updated.isPublished ? 'Homework published' : 'Homework unpublished');
      },
      error: () => this.toast.error('Failed to update status'),
    });
  }

  deleteHomework(hw: HomeworkDto) {
    if (!confirm(`Delete "${hw.title}"?`)) return;
    this.homeworkService.delete(hw.id).subscribe({
      next: () => {
        this.homework.update(list => list.filter(h => h.id !== hw.id));
        this.totalElements.update(n => n - 1);
        this.toast.success('Homework deleted');
      },
      error: () => this.toast.error('Failed to delete homework'),
    });
  }

  dueBadge(days: number): string {
    if (days < 0)  return 'overdue';
    if (days === 0) return 'due-today';
    if (days <= 2)  return 'due-soon';
    return 'upcoming';
  }

  dueLabel(days: number): string {
    if (days < 0)   return `Overdue by ${Math.abs(days)}d`;
    if (days === 0) return 'Due today';
    if (days === 1) return 'Due tomorrow';
    return `Due in ${days}d`;
  }
}
