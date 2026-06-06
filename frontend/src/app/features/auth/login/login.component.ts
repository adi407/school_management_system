import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { ToastService } from '../../../core/services/toast.service';
import { Role } from '../../../core/models/user.model';

@Component({
  selector: 'sms-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);
  private toast  = inject(ToastService);

  theme     = inject(ThemeService);
  loading   = signal(false);
  showPass  = signal(false);
  error     = signal('');

  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set('');

    const { email, password } = this.form.value;
    this.auth.login(email!, password!).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.toast.success('Welcome back!');
        this.navigate(res.user.role);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Invalid email or password');
      },
    });
  }

  private navigate(role: Role) {
    const map: Record<Role, string> = {
      SUPER_ADMIN:       '/super-admin/dashboard',
      SCHOOL_ADMIN:      '/admin/dashboard',
      TEACHER:           '/teacher/dashboard',
      STUDENT:           '/student/dashboard',
      PARENT:            '/parent/dashboard',
      ACCOUNTANT:        '/admin/fees',
      LIBRARIAN:         '/admin/dashboard',
      TRANSPORT_MANAGER: '/admin/dashboard',
      HOSTEL_WARDEN:     '/admin/dashboard',
    };
    this.router.navigate([map[role] ?? '/']);
  }

}
