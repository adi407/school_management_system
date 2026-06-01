import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe, NgClass } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { HomeworkService } from '../../../core/services/homework.service';
import { AcademicService } from '../../../core/services/academic.service';
import { ToastService } from '../../../core/services/toast.service';
import { HomeworkDto } from '../../../core/models/homework.model';
import { ClassDto } from '../../../core/models/academic.model';

@Component({
  selector: 'sms-teacher-homework',
  standalone: true,
  imports: [RouterModule, FormsModule, DatePipe, NgClass],
  templateUrl: './teacher-homework.component.html',
  styleUrls: ['./teacher-homework.component.scss'],
})
export class TeacherHomeworkComponent implements OnInit, OnDestroy {
  private homeworkService = inject(HomeworkService);
  private academicService = inject(AcademicService);
  private toast           = inject(ToastService);
  private destroy$        = new Subject<void>();

  homework      = signal<HomeworkDto[]>([]);
  classes       = signal<ClassDto[]>([]);
  loading       = signal(true);
  selectedClass = '';

  upcomingCount  = signal(0);
  overdueCount   = signal(0);

  ngOnInit() {
    this.academicService.listClasses().pipe(takeUntil(this.destroy$)).subscribe(c => {
      this.classes.set(c);
    });
    this.load();
  }

  ngOnDestroy() { this.destroy$.next(); this.destroy$.complete(); }

  load() {
    this.loading.set(true);
    this.homeworkService.list({
      classId: this.selectedClass || undefined,
      size: 50,
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.homework.set(res.content);
        this.upcomingCount.set(res.content.filter(h => h.daysUntilDue >= 0).length);
        this.overdueCount.set(res.content.filter(h => h.daysUntilDue < 0).length);
        this.loading.set(false);
      },
      error: () => { this.toast.error('Failed to load homework'); this.loading.set(false); },
    });
  }

  togglePublish(hw: HomeworkDto) {
    this.homeworkService.setPublished(hw.id, !hw.isPublished).subscribe({
      next: updated => {
        this.homework.update(list => list.map(h => h.id === updated.id ? updated : h));
        this.toast.success(updated.isPublished ? 'Published!' : 'Moved to draft');
      },
      error: () => this.toast.error('Failed to update'),
    });
  }

  dueBadge(days: number) {
    if (days < 0)  return 'overdue';
    if (days === 0) return 'today';
    if (days <= 2)  return 'soon';
    return 'normal';
  }
}
