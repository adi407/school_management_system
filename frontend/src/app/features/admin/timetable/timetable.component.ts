import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TimetableService } from '../../../core/services/timetable.service';
import { AcademicService } from '../../../core/services/academic.service';
import { StaffService } from '../../../core/services/staff.service';
import { ToastService } from '../../../core/services/toast.service';
import { TimetableSlotDto, DAYS_OF_WEEK } from '../../../core/models/timetable.model';
import { ClassDto, SubjectDto, AcademicYearDto } from '../../../core/models/academic.model';
import { StaffDto } from '../../../core/models/staff.model';

@Component({
  selector: 'sms-timetable',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './timetable.component.html',
  styleUrls: ['./timetable.component.scss'],
})
export class TimetableComponent implements OnInit {
  readonly days = DAYS_OF_WEEK;
  readonly periods = [1, 2, 3, 4, 5, 6, 7, 8];

  classes       = signal<ClassDto[]>([]);
  academicYears = signal<AcademicYearDto[]>([]);
  subjects      = signal<SubjectDto[]>([]);
  teachers      = signal<StaffDto[]>([]);
  slots         = signal<TimetableSlotDto[]>([]);

  selectedClass       = signal('');
  selectedAcademicYear = signal('');
  loading = signal(false);
  saving  = signal(false);
  showModal = signal(false);
  editingSlot = signal<TimetableSlotDto | null>(null);

  // Grid: day → period → slot
  grid = computed(() => {
    const map: Record<string, Record<number, TimetableSlotDto>> = {};
    for (const d of this.days) {
      map[d] = {};
      for (const p of this.periods) {
        const slot = this.slots().find(s => s.dayOfWeek === d && s.periodNo === p);
        if (slot) map[d][p] = slot;
      }
    }
    return map;
  });

  form = this.fb.group({
    dayOfWeek:  ['', Validators.required],
    periodNo:   [1, Validators.required],
    subjectId:  [null as string | null],
    teacherId:  [null as string | null],
    startTime:  ['08:00', Validators.required],
    endTime:    ['08:45', Validators.required],
    roomNo:     [''],
  });

  constructor(
    private fb: FormBuilder,
    private timetableService: TimetableService,
    private academicService: AcademicService,
    private staffService: StaffService,
    private toast: ToastService,
  ) {}

  ngOnInit() {
    this.academicService.listClasses().subscribe(c => this.classes.set(c));
    this.academicService.listYears().subscribe(y => {
      this.academicYears.set(y);
      const current = y.find(yr => yr.isCurrent) ?? y[0];
      if (current) this.selectedAcademicYear.set(current.id);
    });
    this.academicService.listSubjects().subscribe(s => this.subjects.set(s));
    this.staffService.list().subscribe(s => this.teachers.set(s.filter(t => t.role === 'TEACHER')));
  }

  loadTimetable() {
    if (!this.selectedClass() || !this.selectedAcademicYear()) return;
    this.loading.set(true);
    this.timetableService.getClassTimetable(this.selectedClass(), this.selectedAcademicYear()).subscribe({
      next: slots => { this.slots.set(slots); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openSlotModal(day: string, period: number) {
    const existing = this.grid()[day]?.[period];
    this.editingSlot.set(existing ?? null);
    this.form.patchValue({
      dayOfWeek: day,
      periodNo:  period,
      subjectId: existing?.subjectId ?? null,
      teacherId: existing?.teacherId ?? null,
      startTime: existing?.startTime ?? this.defaultStart(period),
      endTime:   existing?.endTime   ?? this.defaultEnd(period),
      roomNo:    existing?.roomNo    ?? '',
    });
    this.showModal.set(true);
  }

  closeModal() { this.showModal.set(false); }

  saveSlot() {
    if (this.form.invalid || !this.selectedClass() || !this.selectedAcademicYear()) return;
    const v = this.form.value;
    this.saving.set(true);
    this.timetableService.upsertSlot({
      classId:        this.selectedClass(),
      academicYearId: this.selectedAcademicYear(),
      dayOfWeek:      v.dayOfWeek!,
      periodNo:       v.periodNo!,
      subjectId:      v.subjectId || null,
      teacherId:      v.teacherId || null,
      startTime:      v.startTime!,
      endTime:        v.endTime!,
      roomNo:         v.roomNo || null,
    }).subscribe({
      next: slot => {
        this.slots.update(list => {
          const idx = list.findIndex(s => s.dayOfWeek === slot.dayOfWeek && s.periodNo === slot.periodNo);
          return idx >= 0 ? list.map((s, i) => i === idx ? slot : s) : [...list, slot];
        });
        this.saving.set(false); this.closeModal();
        this.toast.success('Slot saved');
      },
      error: () => { this.saving.set(false); this.toast.error('Failed to save slot'); },
    });
  }

  deleteSlot() {
    const slot = this.editingSlot();
    if (!slot) return;
    this.timetableService.deleteSlot(slot.id).subscribe({
      next: () => {
        this.slots.update(list => list.filter(s => s.id !== slot.id));
        this.closeModal();
        this.toast.success('Slot cleared');
      },
      error: () => this.toast.error('Delete failed'),
    });
  }

  dayLabel(d: string) {
    const m: Record<string, string> = { MON: 'Monday', TUE: 'Tuesday', WED: 'Wednesday', THU: 'Thursday', FRI: 'Friday', SAT: 'Saturday' };
    return m[d] ?? d;
  }

  private defaultStart(p: number) {
    const base = 8 * 60 + (p - 1) * 45;
    return `${String(Math.floor(base / 60)).padStart(2, '0')}:${String(base % 60).padStart(2, '0')}`;
  }
  private defaultEnd(p: number) {
    const base = 8 * 60 + p * 45 - 5;
    return `${String(Math.floor(base / 60)).padStart(2, '0')}:${String(base % 60).padStart(2, '0')}`;
  }

  get f() { return this.form.controls; }
}
