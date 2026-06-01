import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AcademicService } from '../../../../core/services/academic.service';
import { StaffService } from '../../../../core/services/staff.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ClassDto } from '../../../../core/models/academic.model';
import { StaffDto } from '../../../../core/models/staff.model';

@Component({
  selector: 'sms-class-list',
  standalone: true,
  imports: [RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './class-list.component.html',
  styleUrls: ['./class-list.component.scss'],
})
export class ClassListComponent implements OnInit {
  private academicSvc = inject(AcademicService);
  private staffSvc    = inject(StaffService);
  private toast       = inject(ToastService);
  private fb          = inject(FormBuilder);

  classes   = signal<ClassDto[]>([]);
  teachers  = signal<StaffDto[]>([]);
  loading   = signal(false);
  saving    = signal(false);
  showModal = signal(false);
  editingId = signal<string | null>(null);

  search = signal('');
  filtered = computed(() => {
    const q = this.search().toLowerCase();
    return this.classes().filter(c =>
      !q || c.name.toLowerCase().includes(q) || String(c.grade).includes(q) || c.section.toLowerCase().includes(q)
    );
  });

  form = this.fb.group({
    grade:          [1, [Validators.required, Validators.min(1), Validators.max(13)]],
    section:        ['A', Validators.required],
    name:           ['', Validators.required],
    capacity:       [40, [Validators.min(1)]],
    classTeacherId: [null as string | null],
  });

  get f() { return this.form.controls; }

  ngOnInit() {
    this.loadClasses();
    this.loadTeachers();
  }

  loadClasses() {
    this.loading.set(true);
    this.academicSvc.listClasses().subscribe({
      next: data => { this.classes.set(data); this.loading.set(false); },
      error: ()   => { this.loading.set(false); this.toast.error('Failed to load classes'); },
    });
  }

  loadTeachers() {
    this.staffSvc.list().subscribe({
      next: s => this.teachers.set(s.filter(t => t.role === 'TEACHER' && t.isActive)),
    });
  }

  openCreate() {
    this.editingId.set(null);
    this.form.reset({ grade: 1, section: 'A', capacity: 40, classTeacherId: null });
    // Auto-generate name when grade/section change
    this.showModal.set(true);
  }

  openEdit(cls: ClassDto) {
    this.editingId.set(cls.id);
    this.form.patchValue({
      grade:          cls.grade,
      section:        cls.section,
      name:           cls.name,
      capacity:       cls.capacity,
      classTeacherId: cls.classTeacherId ?? null,
    });
    this.showModal.set(true);
  }

  closeModal() { this.showModal.set(false); }

  autoName() {
    const g = this.form.value.grade;
    const s = this.form.value.section;
    if (g && s) {
      this.form.patchValue({ name: `Grade ${g} - ${s}` });
    }
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const payload = {
      grade:          v.grade!,
      section:        v.section!,
      name:           v.name!,
      capacity:       v.capacity ?? 40,
      classTeacherId: v.classTeacherId ?? undefined,
    };

    this.saving.set(true);
    const editing = this.editingId();

    if (editing) {
      this.academicSvc.updateClass(editing, payload).subscribe({
        next: updated => {
          this.classes.update(list => list.map(c => c.id === editing ? updated : c));
          this.saving.set(false); this.closeModal();
          this.toast.success('Class updated');
        },
        error: err => { this.saving.set(false); this.toast.error(err?.error?.message ?? 'Update failed'); },
      });
    } else {
      this.academicSvc.createClass(payload).subscribe({
        next: created => {
          this.classes.update(list => [...list, created]);
          this.saving.set(false); this.closeModal();
          this.toast.success('Class created');
        },
        error: err => { this.saving.set(false); this.toast.error(err?.error?.message ?? 'Failed to create class'); },
      });
    }
  }

  teacherName(cls: ClassDto): string {
    return cls.classTeacherName ?? '—';
  }
}
