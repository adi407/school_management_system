import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../../core/services/theme.service';
import { RegistrationService } from '../../../core/services/registration.service';

@Component({
  selector: 'sms-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private registrationService = inject(RegistrationService);
  theme = inject(ThemeService);

  submitted = signal(false);
  submitting = signal(false);
  error = signal('');
  step = signal(1);

  form = this.fb.nonNullable.group({
    schoolName: ['', Validators.required],
    schoolCode: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20), Validators.pattern(/^[A-Za-z0-9_-]+$/)]],
    board: ['CBSE', Validators.required],
    requestedTier: ['FREE'],
    address: [''],
    city: [''],
    state: [''],
    phone: [''],
    schoolEmail: ['', Validators.email],
    website: [''],
    studentCount: [null as number | null],
    adminName: ['', Validators.required],
    adminEmail: ['', [Validators.required, Validators.email]],
    adminPhone: [''],
    adminDesignation: [''],
    message: [''],
  });

  nextStep() {
    const schoolFields = ['schoolName', 'schoolCode', 'board'] as const;
    let valid = true;
    for (const f of schoolFields) {
      const ctrl = this.form.controls[f];
      ctrl.markAsTouched();
      if (ctrl.invalid) valid = false;
    }
    if (valid) this.step.set(2);
  }

  prevStep() {
    this.step.set(1);
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set('');

    const val = this.form.getRawValue();
    this.registrationService.submitRegistration({
      ...val,
      schoolCode: val.schoolCode.toUpperCase(),
      studentCount: val.studentCount ?? undefined,
    }).subscribe({
      next: () => {
        this.submitted.set(true);
        this.submitting.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || err.error?.error || 'Something went wrong. Please try again.';
        this.error.set(msg);
        this.submitting.set(false);
      },
    });
  }

  generateCode() {
    const name = this.form.controls.schoolName.value;
    if (!name) return;
    const code = name
      .toUpperCase()
      .replace(/[^A-Z0-9 ]/g, '')
      .split(' ')
      .filter(Boolean)
      .map((w: string) => w[0])
      .join('')
      .slice(0, 8);
    if (code.length >= 3) {
      this.form.controls.schoolCode.setValue(code);
    }
  }
}
