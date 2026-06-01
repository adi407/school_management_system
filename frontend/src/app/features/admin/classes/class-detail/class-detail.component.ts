import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AcademicService } from '../../../../core/services/academic.service';
import { StaffService } from '../../../../core/services/staff.service';
import { StudentService } from '../../../../core/services/student.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ClassDto, ClassSubjectDto, SubjectDto } from '../../../../core/models/academic.model';
import { StaffDto } from '../../../../core/models/staff.model';
import { StudentSummaryDto } from '../../../../core/models/student.model';

type Tab = 'overview' | 'subjects' | 'students';

@Component({
  selector: 'sms-class-detail',
  standalone: true,
  imports: [RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './class-detail.component.html',
  styleUrls: ['./class-detail.component.scss'],
})
export class ClassDetailComponent implements OnInit {
  private route       = inject(ActivatedRoute);
  private academicSvc = inject(AcademicService);
  private staffSvc    = inject(StaffService);
  private studentSvc  = inject(StudentService);
  private toast       = inject(ToastService);
  private fb          = inject(FormBuilder);

  classId = '';
  activeTab = signal<Tab>('overview');
  loading   = signal(true);
  saving    = signal(false);

  // ── Data ──────────────────────────────────────────────────────────────────
  cls       = signal<ClassDto | null>(null);
  subjects  = signal<ClassSubjectDto[]>([]);
  students  = signal<StudentSummaryDto[]>([]);
  teachers  = signal<StaffDto[]>([]);
  allSubjects = signal<SubjectDto[]>([]);

  // subjects not yet assigned to this class
  availableSubjects = computed(() => {
    const assigned = new Set(this.subjects().map(s => s.subjectId));
    return this.allSubjects().filter(s => !assigned.has(s.id));
  });

  // ── Overview form ─────────────────────────────────────────────────────────
  overviewForm = this.fb.group({
    name:           ['', Validators.required],
    capacity:       [40, [Validators.required, Validators.min(1)]],
    classTeacherId: [null as string | null],
  });
  get of() { return this.overviewForm.controls; }

  // ── Assign Subject modal ──────────────────────────────────────────────────
  showAssignModal = signal(false);
  assignForm = this.fb.group({
    subjectId: ['', Validators.required],
    teacherId: [null as string | null],
  });
  get af() { return this.assignForm.controls; }

  // ── Change Teacher modal ──────────────────────────────────────────────────
  showTeacherModal = signal(false);
  editingCst = signal<ClassSubjectDto | null>(null);
  teacherForm = this.fb.group({
    teacherId: [null as string | null],
  });

  ngOnInit() {
    this.classId = this.route.snapshot.paramMap.get('id')!;
    this.loadAll();
  }

  loadAll() {
    this.loading.set(true);
    forkJoin({
      cls:      this.academicSvc.getClass(this.classId),
      subjects: this.academicSvc.listClassSubjects(this.classId),
      allSubs:  this.academicSvc.listSubjects(),
      teachers: this.staffSvc.list(),
    }).subscribe({
      next: ({ cls, subjects, allSubs, teachers }) => {
        this.cls.set(cls);
        this.subjects.set(subjects);
        this.allSubjects.set(allSubs);
        this.teachers.set(teachers.filter(t => t.role === 'TEACHER' && t.isActive));

        this.overviewForm.patchValue({
          name:           cls.name,
          capacity:       cls.capacity,
          classTeacherId: cls.classTeacherId ?? null,
        });

        this.loading.set(false);
      },
      error: () => { this.loading.set(false); this.toast.error('Failed to load class data'); },
    });
  }

  loadStudents() {
    if (this.students().length > 0) return; // already loaded
    this.studentSvc.list({ classId: this.classId, size: 200 }).subscribe({
      next: page => this.students.set(page.content),
      error: ()  => this.toast.error('Failed to load students'),
    });
  }

  setTab(tab: Tab) {
    this.activeTab.set(tab);
    if (tab === 'students') this.loadStudents();
  }

  // ── Overview ───────────────────────────────────────────────────────────────
  saveOverview() {
    if (this.overviewForm.invalid) { this.overviewForm.markAllAsTouched(); return; }
    const v = this.overviewForm.getRawValue();
    this.saving.set(true);
    this.academicSvc.updateClass(this.classId, {
      name:           v.name!,
      capacity:       v.capacity!,
      classTeacherId: v.classTeacherId ?? undefined,
    }).subscribe({
      next: updated => {
        this.cls.set(updated);
        this.saving.set(false);
        this.toast.success('Class updated');
      },
      error: err => { this.saving.set(false); this.toast.error(err?.error?.message ?? 'Update failed'); },
    });
  }

  // ── Assign Subject ────────────────────────────────────────────────────────
  openAssign() {
    this.assignForm.reset({ subjectId: '', teacherId: null });
    this.showAssignModal.set(true);
  }

  closeAssign() { this.showAssignModal.set(false); }

  submitAssign() {
    if (this.assignForm.invalid) { this.assignForm.markAllAsTouched(); return; }
    const v = this.assignForm.getRawValue();
    this.saving.set(true);
    this.academicSvc.assignSubject(this.classId, {
      subjectId: v.subjectId!,
      teacherId: v.teacherId ?? null,
    }).subscribe({
      next: cst => {
        this.subjects.update(list => [...list, cst]);
        this.saving.set(false); this.closeAssign();
        this.toast.success('Subject assigned');
      },
      error: err => { this.saving.set(false); this.toast.error(err?.error?.message ?? 'Failed to assign subject'); },
    });
  }

  // ── Change Teacher ────────────────────────────────────────────────────────
  openTeacher(cst: ClassSubjectDto) {
    this.editingCst.set(cst);
    this.teacherForm.patchValue({ teacherId: cst.teacherId ?? null });
    this.showTeacherModal.set(true);
  }

  closeTeacher() { this.showTeacherModal.set(false); this.editingCst.set(null); }

  submitTeacher() {
    const cst = this.editingCst();
    if (!cst) return;
    const teacherId = this.teacherForm.value.teacherId ?? null;
    this.saving.set(true);
    this.academicSvc.updateSubjectTeacher(this.classId, cst.id, teacherId).subscribe({
      next: updated => {
        this.subjects.update(list => list.map(s => s.id === cst.id ? updated : s));
        this.saving.set(false); this.closeTeacher();
        this.toast.success('Teacher updated');
      },
      error: err => { this.saving.set(false); this.toast.error(err?.error?.message ?? 'Failed to update teacher'); },
    });
  }

  // ── Remove Subject ────────────────────────────────────────────────────────
  removeSubject(cst: ClassSubjectDto) {
    if (!confirm(`Remove "${cst.subjectName}" from this class?`)) return;
    this.academicSvc.removeSubject(this.classId, cst.id).subscribe({
      next: () => {
        this.subjects.update(list => list.filter(s => s.id !== cst.id));
        this.toast.success('Subject removed');
      },
      error: err => this.toast.error(err?.error?.message ?? 'Failed to remove subject'),
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────────
  subjectTypeBadge(type: string): string {
    const map: Record<string, string> = {
      CORE: 'blue', ELECTIVE: 'green', ACTIVITY: 'orange', LANGUAGE: 'purple'
    };
    return map[type] ?? 'gray';
  }

  initials(name: string): string {
    return (name ?? '?').split(/\s+/).map(p => p[0] ?? '').join('').toUpperCase().slice(0, 2);
  }

  fillPct(): number {
    const c = this.cls();
    if (!c || c.capacity === 0) return 0;
    return Math.min(100, Math.round(c.studentCount / c.capacity * 100));
  }
}
