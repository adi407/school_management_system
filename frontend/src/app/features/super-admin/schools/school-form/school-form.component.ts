import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { SchoolService } from '../../../../core/services/school.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'sms-school-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './school-form.component.html',
  styleUrls: ['./school-form.component.scss'],
})
export class SchoolFormComponent {
  private fb        = inject(FormBuilder);
  private schoolSvc = inject(SchoolService);
  private toast     = inject(ToastService);
  private router    = inject(Router);

  step    = signal(1);
  loading = signal(false);

  form = this.fb.group({
    // Step 1 — School details
    name:               ['', Validators.required],
    code:               ['', [Validators.required, Validators.pattern(/^[A-Z0-9]{2,10}$/i)]],
    board:              ['CBSE', Validators.required],
    subscriptionTier:   ['FREE', Validators.required],
    address:            [''],
    phone:              [''],
    email:              ['', Validators.email],
    timezone:           ['Asia/Kolkata'],
    subscriptionExpiry: [''],
    // Step 2 — Admin account
    adminEmail:    ['', [Validators.required, Validators.email]],
    adminPassword: ['', [Validators.required, Validators.minLength(8)]],
    adminName:     [''],
  });

  nextStep() {
    const step1Fields = ['name', 'code', 'board', 'subscriptionTier'];
    step1Fields.forEach(f => this.form.get(f)?.markAsTouched());
    if (step1Fields.some(f => this.form.get(f)?.invalid)) return;
    this.step.set(2);
  }

  submit() {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    this.loading.set(true);

    const v = this.form.value;
    this.schoolSvc.createSchool({
      name:             v.name!,
      code:             v.code!.toUpperCase(),
      board:            v.board as any,
      subscriptionTier: v.subscriptionTier as any,
      address:          v.address || undefined,
      phone:            v.phone   || undefined,
      email:            v.email   || undefined,
      timezone:         v.timezone || undefined,
      subscriptionExpiry: v.subscriptionExpiry || undefined,
      adminEmail:    v.adminEmail!,
      adminPassword: v.adminPassword!,
      adminName:     v.adminName  || undefined,
    }).subscribe({
      next: (school) => {
        this.loading.set(false);
        this.toast.success(`School "${school.name}" enrolled successfully!`);
        this.router.navigate(['/super-admin/schools']);
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.error(err?.error?.message ?? 'Failed to create school');
      },
    });
  }

  field(name: string) { return this.form.get(name); }
  isInvalid(name: string) { const f = this.field(name); return f?.invalid && f?.touched; }
}
