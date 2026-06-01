import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StudentService } from '../../../../core/services/student.service';
import { AcademicService } from '../../../../core/services/academic.service';
import { ToastService } from '../../../../core/services/toast.service';
import { CreateStudentRequest, StudentCategory, Gender } from '../../../../core/models/student.model';
import { ClassDto } from '../../../../core/models/academic.model';

type Step = 1 | 2 | 3 | 4 | 5;

@Component({
  selector: 'sms-student-form',
  standalone: true,
  imports: [RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './student-form.component.html',
  styleUrls: ['./student-form.component.scss'],
})
export class StudentFormComponent implements OnInit {
  private fb              = inject(FormBuilder);
  private router          = inject(Router);
  private studentService  = inject(StudentService);
  private academicService = inject(AcademicService);
  private toast           = inject(ToastService);

  currentStep = signal<Step>(1);
  submitting  = signal(false);
  error       = signal('');

  steps = [
    { n: 1, label: 'Personal Info' },
    { n: 2, label: 'Academic'      },
    { n: 3, label: 'Guardian'      },
    { n: 4, label: 'Medical'       },
    { n: 5, label: 'Review'        },
  ];

  classes: ClassDto[] = [];

  ngOnInit() {
    this.academicService.listClasses().subscribe({
      next: cls => (this.classes = cls),
      error: () => { /* optional: show toast */ }
    });
  }

  personalForm: FormGroup = this.fb.group({
    firstName:   ['', [Validators.required, Validators.minLength(2)]],
    lastName:    ['', [Validators.required, Validators.minLength(2)]],
    dateOfBirth: ['', Validators.required],
    gender:      ['', Validators.required],
    category:    ['GEN', Validators.required],
    bloodGroup:  [''],
    nationality: ['Indian'],
    religion:    [''],
    caste:       [''],
    motherTongue:[''],
    aadhaarNo:   [''],
  });

  academicForm: FormGroup = this.fb.group({
    classId:      [''],
    rollNo:       [''],
    houseGroup:   [''],
    admissionDate:[new Date().toISOString().split('T')[0], Validators.required],
  });

  guardianForm: FormGroup = this.fb.group({
    guardianName:     ['', Validators.required],
    guardianPhone:    ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
    guardianEmail:    ['', Validators.email],
    guardianRelation: ['FATHER', Validators.required],
  });

  medicalForm: FormGroup = this.fb.group({
    allergies:        [''],
    chronicCondition: [''],
    emergencyContact: [''],
    medicalNotes:     [''],
  });

  stepValid = computed(() => {
    switch (this.currentStep()) {
      case 1: return this.personalForm.valid;
      case 2: return this.academicForm.valid;
      case 3: return this.guardianForm.valid;
      case 4: return true;
      case 5: return true;
      default: return false;
    }
  });

  get pf() { return this.personalForm.controls; }
  get af() { return this.academicForm.controls; }
  get gf() { return this.guardianForm.controls; }

  selectedClassName = computed(() => {
    const id  = this.af['classId'].value;
    const cls = this.classes.find(c => c.id === id);
    return cls?.name ?? '(No class selected)';
  });

  next() { if (this.currentStep() < 5) this.currentStep.update(s => (s + 1) as Step); }
  prev() { if (this.currentStep() > 1) this.currentStep.update(s => (s - 1) as Step); }

  goToStep(n: number) {
    if (n < this.currentStep()) this.currentStep.set(n as Step);
  }

  submit() {
    this.submitting.set(true);
    this.error.set('');

    const pv = this.personalForm.value;
    const av = this.academicForm.value;
    const gv = this.guardianForm.value;
    const mv = this.medicalForm.value;

    // Build medical conditions JSON string
    const medicalParts: Record<string, string> = {};
    if (mv.allergies)        medicalParts['allergies']        = mv.allergies;
    if (mv.chronicCondition) medicalParts['chronicCondition'] = mv.chronicCondition;
    if (mv.emergencyContact) medicalParts['emergencyContact'] = mv.emergencyContact;
    if (mv.medicalNotes)     medicalParts['notes']            = mv.medicalNotes;
    const medStr = Object.keys(medicalParts).length
      ? JSON.stringify(medicalParts)
      : undefined;

    const req: CreateStudentRequest = {
      firstName:        pv.firstName,
      lastName:         pv.lastName,
      dateOfBirth:      pv.dateOfBirth,
      gender:           pv.gender as Gender,
      category:         pv.category as StudentCategory,
      bloodGroup:       pv.bloodGroup   || undefined,
      nationality:      pv.nationality  || 'Indian',
      religion:         pv.religion     || undefined,
      caste:            pv.caste        || undefined,
      motherTongue:     pv.motherTongue || undefined,
      aadhaarNo:        pv.aadhaarNo    || undefined,
      classId:          av.classId      || undefined,
      rollNo:           av.rollNo       || undefined,
      houseGroup:       av.houseGroup   || undefined,
      admissionDate:    av.admissionDate,
      medicalConditions: medStr,
      guardians: [{
        name:              gv.guardianName,
        relation:          gv.guardianRelation,
        phone:             gv.guardianPhone,
        email:             gv.guardianEmail || undefined,
        isPrimary:         true,
        isAuthorizedPickup:true,
      }],
    };

    this.studentService.create(req).subscribe({
      next: student => {
        this.submitting.set(false);
        this.toast.success(`Student "${student.fullName}" enrolled successfully!`);
        this.router.navigate(['/admin/students', student.id]);
      },
      error: err => {
        this.submitting.set(false);
        const msg = err.error?.message ?? err.error?.error ?? 'Failed to enroll student';
        this.error.set(msg);
      },
    });
  }

  fieldError(form: FormGroup, field: string): string {
    const ctrl = form.get(field);
    if (!ctrl?.dirty || !ctrl.errors) return '';
    if (ctrl.errors['required'])  return 'This field is required.';
    if (ctrl.errors['minlength']) return `Minimum ${ctrl.errors['minlength'].requiredLength} characters.`;
    if (ctrl.errors['email'])     return 'Enter a valid email address.';
    if (ctrl.errors['pattern'])   return 'Enter a valid 10-digit mobile number.';
    return 'Invalid value.';
  }
}
