import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ThemeService } from '../../../core/services/theme.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'sms-request-demo',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './request-demo.component.html',
  styleUrls: ['./request-demo.component.scss'],
})
export class RequestDemoComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  theme = inject(ThemeService);

  submitted = signal(false);
  submitting = signal(false);
  error = signal('');

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    schoolName: [''],
    city: [''],
    role: [''],
    message: [''],
  });

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set('');
    this.http.post(`${environment.apiUrl}/public/demo-requests`, this.form.getRawValue())
      .subscribe({
        next: () => {
          this.submitted.set(true);
          this.submitting.set(false);
        },
        error: () => {
          this.error.set('Something went wrong. Please try again.');
          this.submitting.set(false);
        },
      });
  }
}
