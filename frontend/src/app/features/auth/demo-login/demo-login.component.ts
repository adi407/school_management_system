import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Role } from '../../../core/models/user.model';

// ── Change this to rotate the PIN ──────────────────────────────────────────
const DEMO_PIN = '02121974';

@Component({
  selector: 'sms-demo-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './demo-login.component.html',
  styleUrls: ['./demo-login.component.scss'],
})
export class DemoLoginComponent implements OnInit {
  private auth   = inject(AuthService);
  private router = inject(Router);
  private toast  = inject(ToastService);

  // ── PIN gate ───────────────────────────────────────────────────────────────
  pinValue   = '';        // bound via ngModel — string keeps leading zeros
  pinUnlocked = signal(false);
  pinError    = signal(false);
  pinShake    = signal(false);

  // ── Demo role cards ────────────────────────────────────────────────────────
  loading = signal<string | null>(null);  // email of the card currently signing in

  readonly roles: { label: string; email: string; role: string; icon: string; desc: string }[] = [
    { label: 'Super Admin',  email: 'superadmin@educloud.com',  role: 'SUPER_ADMIN',  icon: '👑', desc: 'Full platform control · all schools' },
    { label: 'School Admin', email: 'admin@schoolmanager.live', role: 'SCHOOL_ADMIN', icon: '🏫', desc: 'Staff, students, fees, modules'        },
    { label: 'Teacher',      email: 'priya.sharma@demo.school', role: 'TEACHER',      icon: '📖', desc: 'Classes, homework & attendance'         },
    { label: 'Student',      email: 'student@demo.school',      role: 'STUDENT',      icon: '🎓', desc: 'Timetable, homework, updates'           },
  ];

  // ── Lifecycle ──────────────────────────────────────────────────────────────
  ngOnInit() {
    // Already logged in → bounce to their dashboard
    if (this.auth.isLoggedIn()) this.navigate(this.auth.role()!);
  }

  // ── PIN logic ──────────────────────────────────────────────────────────────
  checkPin() {
    if (this.pinValue === DEMO_PIN) {
      this.pinUnlocked.set(true);
      this.pinError.set(false);
    } else {
      this.pinError.set(true);
      this.pinShake.set(true);
      setTimeout(() => this.pinShake.set(false), 600);
    }
  }

  onPinKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter') this.checkPin();
  }

  // ── One-click demo login ───────────────────────────────────────────────────
  loginAs(email: string) {
    if (this.loading()) return;
    this.loading.set(email);

    this.auth.login(email, 'Admin@1234').subscribe({
      next: res => {
        this.loading.set(null);
        this.toast.success(`Demo: signed in as ${res.user.role.replace(/_/g, ' ')}`);
        this.navigate(res.user.role);
      },
      error: err => {
        this.loading.set(null);
        this.toast.error(err?.error?.message ?? 'Login failed — check demo credentials');
      },
    });
  }

  // ── Navigation map (mirrors LoginComponent) ────────────────────────────────
  private navigate(role: Role) {
    const map: Partial<Record<Role, string>> = {
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
