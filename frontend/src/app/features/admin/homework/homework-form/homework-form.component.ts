import { Component, signal, inject, OnInit } from '@angular/core';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HomeworkService } from '../../../../core/services/homework.service';
import { AcademicService } from '../../../../core/services/academic.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ClassDto, SubjectDto, AcademicYearDto } from '../../../../core/models/academic.model';
import { HomeworkDto } from '../../../../core/models/homework.model';

@Component({
  selector: 'sms-homework-form',
  standalone: true,
  imports: [RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './homework-form.component.html',
  styleUrls: ['./homework-form.component.scss'],
})
export class HomeworkFormComponent implements OnInit {
  private fb              = inject(FormBuilder);
  private router          = inject(Router);
  private route           = inject(ActivatedRoute);
  private homeworkService = inject(HomeworkService);
  private academicService = inject(AcademicService);
  private toast           = inject(ToastService);

  isEdit    = signal(false);
  editId    = signal<string | null>(null);
  loading   = signal(false);
  submitting = signal(false);
  error     = signal('');

  /** Back/cancel URL — injected via route data so teachers go to /teacher/homework */
  backUrl   = signal('/admin/homework');

  classes      = signal<ClassDto[]>([]);
  subjects     = signal<SubjectDto[]>([]);
  academicYears = signal<AcademicYearDto[]>([]);

  // Tomorrow's date as default due date
  private tomorrow = (() => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
  })();

  form: FormGroup = this.fb.group({
    isSchoolWide:     [false],
    classId:          ['', Validators.required],
    subjectId:        [''],
    academicYearId:   [''],
    title:            ['', [Validators.required, Validators.minLength(5), Validators.maxLength(300)]],
    description:      ['', [Validators.required, Validators.minLength(10)]],
    dueDate:          [this.tomorrow, Validators.required],
    estimatedMinutes: [null],
    isPublished:      [true],
  });

  ngOnInit() {
    // Read back URL from route data (teachers use /teacher/homework, admins use /admin/homework)
    const routeBackUrl = this.route.snapshot.data['backUrl'];
    if (routeBackUrl) this.backUrl.set(routeBackUrl);

    // Load dropdown data in parallel
    this.academicService.listClasses().subscribe(c => this.classes.set(c));
    this.academicService.listSubjects().subscribe(s => this.subjects.set(s));
    this.academicService.listYears().subscribe(y => this.academicYears.set(y));

    // Toggle class required-ness based on school-wide flag
    this.form.get('isSchoolWide')!.valueChanges.subscribe((schoolWide: boolean) => {
      const classCtrl = this.form.get('classId')!;
      if (schoolWide) {
        classCtrl.clearValidators();
        classCtrl.setValue('');
      } else {
        classCtrl.setValidators(Validators.required);
      }
      classCtrl.updateValueAndValidity();
    });

    // Edit mode: load existing homework
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.editId.set(id);
      this.loading.set(true);
      this.homeworkService.get(id).subscribe({
        next: hw => {
          this.patchForm(hw);
          this.loading.set(false);
        },
        error: () => {
          this.toast.error('Could not load homework');
          this.router.navigate([this.backUrl()]);
        },
      });
    }
  }

  private patchForm(hw: HomeworkDto) {
    this.form.patchValue({
      isSchoolWide:     hw.isSchoolWide,
      classId:          hw.classId ?? '',
      subjectId:        hw.subjectId ?? '',
      academicYearId:   '',
      title:            hw.title,
      description:      hw.description,
      dueDate:          hw.dueDate,
      estimatedMinutes: hw.estimatedMinutes,
      isPublished:      hw.isPublished,
    });
  }

  get f() { return this.form.controls; }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting.set(true);
    this.error.set('');

    const v = this.form.value;
    const req = {
      isSchoolWide:     v.isSchoolWide ?? false,
      classId:          v.isSchoolWide ? undefined : (v.classId || undefined),
      subjectId:        v.subjectId   || undefined,
      academicYearId:   v.academicYearId || undefined,
      title:            v.title,
      description:      v.description,
      dueDate:          v.dueDate,
      estimatedMinutes: v.estimatedMinutes || undefined,
      isPublished:      v.isPublished ?? true,
    };

    const obs$ = this.isEdit() && this.editId()
      ? this.homeworkService.update(this.editId()!, req)
      : this.homeworkService.create(req);

    obs$.subscribe({
      next: hw => {
        this.submitting.set(false);
        this.toast.success(this.isEdit() ? 'Homework updated!' : 'Homework created!');
        this.router.navigate([this.backUrl()]);
      },
      error: err => {
        this.submitting.set(false);
        this.error.set(err?.error?.message ?? 'Something went wrong. Please try again.');
      },
    });
  }
}
