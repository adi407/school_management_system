import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'sms-landing',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss'],
})
export class LandingComponent implements OnInit {
  private router = inject(Router);
  private auth   = inject(AuthService);

  features = [
    { icon: '👥', title: 'Student Management', desc: 'Admissions, profiles, guardian links, attendance history and academic records — all in one place.' },
    { icon: '📅', title: 'Smart Timetable', desc: 'Drag-and-drop scheduling with conflict detection. Teachers and students see their personalised view instantly.' },
    { icon: '💰', title: 'Fee Management', desc: 'Automated fee structures, payment tracking, overdue alerts and one-click receipts for every transaction.' },
    { icon: '📝', title: 'Homework & Exams', desc: 'Assign, track and grade homework. Schedule exams with auto-status updates and result management.' },
    { icon: '📚', title: 'Library System', desc: 'Digital catalogue, book issue and return workflows with overdue tracking and borrower history.' },
    { icon: '💙', title: 'Campus Pulse', desc: 'Anonymous student wellness check-ins with mood analytics so staff can act before problems escalate.' },
    { icon: '📢', title: 'Announcements', desc: 'Role-targeted notices with pin support. Reach teachers, students or parents with one click.' },
    { icon: '🏆', title: 'Activities', desc: 'Manage sports teams, clubs and cultural activities with capacity tracking and coach assignments.' },
  ];

  roles = [
    { role: 'School Admin', icon: '🏫', desc: 'Full control: students, staff, finance, timetable and analytics.' },
    { role: 'Teacher', icon: '👨‍🏫', desc: 'Class schedule, homework assignment, attendance and announcements.' },
    { role: 'Student', icon: '🎒', desc: 'Timetable, homework tracker, attendance summary and notices.' },
    { role: 'Parent', icon: '👨‍👩‍👦', desc: "Child's attendance, fee status, homework and school communications." },
  ];

  year = new Date().getFullYear();

  ngOnInit(): void {
    // If already logged in, redirect to dashboard
    if (this.auth.isLoggedIn()) {
      const role = this.auth.role();
      if (role === 'SCHOOL_ADMIN' || role === 'ACCOUNTANT' || role === 'LIBRARIAN') this.router.navigate(['/admin/dashboard']);
      else if (role === 'TEACHER')  this.router.navigate(['/teacher/dashboard']);
      else if (role === 'STUDENT')  this.router.navigate(['/student/dashboard']);
      else if (role === 'PARENT')   this.router.navigate(['/parent/dashboard']);
      else if (role === 'SUPER_ADMIN') this.router.navigate(['/super-admin/dashboard']);
    }
  }
}
