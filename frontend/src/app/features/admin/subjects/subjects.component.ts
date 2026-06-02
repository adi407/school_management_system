import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AcademicService } from '../../../core/services/academic.service';
import { ToastService } from '../../../core/services/toast.service';
import { SubjectDto } from '../../../core/models/academic.model';

const SUBJECT_TYPES = ['CORE', 'ELECTIVE', 'LANGUAGE', 'ACTIVITY'] as const;

@Component({
  selector: 'sms-subjects',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './subjects.component.html',
  styleUrls: ['./subjects.component.scss'],
})
export class SubjectsComponent implements OnInit {
  private svc   = inject(AcademicService);
  private toast = inject(ToastService);
  private fb    = inject(FormBuilder);

  subjects  = signal<SubjectDto[]>([]);
  loading   = signal(false);
  saving    = signal(false);
  showModal = signal(false);
  editingId = signal<string | null>(null);
  search    = signal('');

  readonly subjectTypes = SUBJECT_TYPES;

  filtered = computed(() => {
    const q = this.search().toLowerCase();
    return this.subjects().filter(s =>
      !q || s.name.toLowerCase().includes(q) || s.code.toLowerCase().includes(q) || s.type.toLowerCase().includes(q)
    );
  });

  form = this.fb.group({
    name:        ['', Validators.required],
    code:        ['', Validators.required],
    type:        ['CORE', Validators.required],
    creditHours: [null as number | null],
  });
  get f() { return this.form.controls; }

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.svc.listSubjects().subscribe({
      next: s => { this.subjects.set(s); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load subjects'); },
    });
  }

  openCreate() {
    this.editingId.set(null);
    this.form.reset({ type: 'CORE', creditHours: null });
    this.showModal.set(true);
  }

  openEdit(s: SubjectDto) {
    this.editingId.set(s.id);
    this.form.patchValue({
      name:        s.name,
      code:        s.code,
      type:        s.type,
      creditHours: s.creditHours ?? null,
    });
    this.showModal.set(true);
  }

  closeModal() { this.showModal.set(false); }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const payload = {
      name:        v.name!,
      code:        v.code!.toUpperCase(),
      type:        v.type!,
      creditHours: v.creditHours ?? undefined,
    };
    this.saving.set(true);
    const id = this.editingId();

    const req$ = id
      ? this.svc.updateSubject(id, payload)
      : this.svc.createSubject(payload);

    req$.subscribe({
      next: result => {
        if (id) {
          this.subjects.update(list => list.map(s => s.id === id ? result : s));
        } else {
          this.subjects.update(list => [...list, result]);
        }
        this.saving.set(false);
        this.closeModal();
        this.toast.success(id ? 'Subject updated' : 'Subject created');
      },
      error: err => { this.saving.set(false); this.toast.error(err?.error?.message ?? 'Save failed'); },
    });
  }

  delete(s: SubjectDto) {
    if (!confirm(`Delete subject "${s.name}"? This cannot be undone.`)) return;
    this.svc.deleteSubject(s.id).subscribe({
      next: () => {
        this.subjects.update(list => list.filter(x => x.id !== s.id));
        this.toast.success('Subject deleted');
      },
      error: err => this.toast.error(err?.error?.message ?? 'Delete failed — subject may be in use'),
    });
  }

  typeBadge(type: string): string {
    const m: Record<string, string> = {
      CORE: 'blue', ELECTIVE: 'green', ACTIVITY: 'orange', LANGUAGE: 'purple'
    };
    return m[type] ?? 'gray';
  }
}
