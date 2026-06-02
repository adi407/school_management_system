import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent),
    pathMatch: 'full',
  },

  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },

  {
    path: 'unauthorized',
    loadComponent: () => import('./shared/components/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent),
  },

  {
    path: 'upgrade',
    loadComponent: () => import('./shared/components/upgrade/upgrade.component').then(m => m.UpgradeComponent),
  },

  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      // ── Super Admin ──────────────────────────────────────────
      {
        path: 'super-admin',
        canActivate: [roleGuard(['SUPER_ADMIN'])],
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/super-admin/dashboard/sa-dashboard.component').then(m => m.SaDashboardComponent),
          },
          {
            path: 'schools',
            loadComponent: () => import('./features/super-admin/schools/school-list/school-list.component').then(m => m.SchoolListComponent),
          },
          {
            path: 'schools/new',
            loadComponent: () => import('./features/super-admin/schools/school-form/school-form.component').then(m => m.SchoolFormComponent),
          },
          {
            path: 'schools/:id/features',
            loadComponent: () => import('./features/super-admin/schools/school-features/school-features.component').then(m => m.SchoolFeaturesComponent),
          },
          { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        ],
      },

      // ── School Admin ──────────────────────────────────────────
      {
        path: 'admin',
        canActivate: [roleGuard(['SCHOOL_ADMIN', 'ACCOUNTANT', 'LIBRARIAN'])],
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/admin/dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
          },
          {
            path: 'students',
            loadComponent: () => import('./features/admin/students/student-list/student-list.component').then(m => m.StudentListComponent),
          },
          {
            path: 'students/new',
            loadComponent: () => import('./features/admin/students/student-form/student-form.component').then(m => m.StudentFormComponent),
          },
          {
            path: 'students/:id',
            loadComponent: () => import('./features/admin/students/student-profile/student-profile.component').then(m => m.StudentProfileComponent),
          },
          {
            path: 'attendance',
            loadComponent: () => import('./features/admin/attendance/attendance.component').then(m => m.AttendanceComponent),
          },
          {
            path: 'timetable',
            loadComponent: () => import('./features/admin/timetable/timetable.component').then(m => m.TimetableComponent),
          },
          {
            path: 'exams',
            loadComponent: () => import('./features/admin/exams/exams.component').then(m => m.ExamsComponent),
          },
          {
            path: 'fees',
            loadComponent: () => import('./features/admin/fees/fees.component').then(m => m.FeesComponent),
          },
          {
            path: 'activities',
            loadComponent: () => import('./features/admin/activities/activities.component').then(m => m.ActivitiesComponent),
          },
          {
            path: 'staff',
            loadComponent: () => import('./features/admin/staff/staff.component').then(m => m.StaffComponent),
          },
          {
            path: 'library',
            loadComponent: () => import('./features/admin/library/library.component').then(m => m.LibraryComponent),
          },
          // ── Academic Setup ────────────────────────────────────
          {
            path: 'academic-years',
            loadComponent: () => import('./features/admin/academic-years/academic-years.component').then(m => m.AcademicYearsComponent),
          },
          {
            path: 'subjects',
            loadComponent: () => import('./features/admin/subjects/subjects.component').then(m => m.SubjectsComponent),
          },
          // ── Classes ───────────────────────────────────────────
          {
            path: 'classes',
            loadComponent: () => import('./features/admin/classes/class-list/class-list.component').then(m => m.ClassListComponent),
          },
          {
            path: 'classes/:id',
            loadComponent: () => import('./features/admin/classes/class-detail/class-detail.component').then(m => m.ClassDetailComponent),
          },
          // ── Homework ──────────────────────────────────────────
          {
            path: 'homework',
            loadComponent: () => import('./features/admin/homework/homework-list/homework-list.component').then(m => m.HomeworkListComponent),
          },
          {
            path: 'homework/new',
            loadComponent: () => import('./features/admin/homework/homework-form/homework-form.component').then(m => m.HomeworkFormComponent),
          },
          {
            path: 'homework/:id/edit',
            loadComponent: () => import('./features/admin/homework/homework-form/homework-form.component').then(m => m.HomeworkFormComponent),
          },
          // ── Campus Pulse ──────────────────────────────────────
          {
            path: 'pulse',
            loadComponent: () => import('./features/admin/wellness/campus-pulse.component').then(m => m.CampusPulseComponent),
          },
          {
            path: 'announcements',
            loadComponent: () => import('./features/admin/announcements/announcements.component').then(m => m.AnnouncementsComponent),
          },
          { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        ],
      },

      // ── Teacher ──────────────────────────────────────────────
      {
        path: 'teacher',
        canActivate: [roleGuard(['TEACHER'])],
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/teacher/teacher-dashboard/teacher-dashboard.component').then(m => m.TeacherDashboardComponent),
          },
          {
            path: 'homework',
            loadComponent: () => import('./features/teacher/homework/teacher-homework.component').then(m => m.TeacherHomeworkComponent),
          },
          {
            path: 'attendance',
            loadComponent: () => import('./features/admin/attendance/attendance.component').then(m => m.AttendanceComponent),
          },
          {
            path: 'announcements',
            loadComponent: () => import('./features/teacher/announcements/teacher-announcements.component').then(m => m.TeacherAnnouncementsComponent),
          },
          {
            path: 'homework/new',
            loadComponent: () => import('./features/admin/homework/homework-form/homework-form.component').then(m => m.HomeworkFormComponent),
            data: { backUrl: '/teacher/homework' },
          },
          {
            path: 'homework/:id/edit',
            loadComponent: () => import('./features/admin/homework/homework-form/homework-form.component').then(m => m.HomeworkFormComponent),
            data: { backUrl: '/teacher/homework' },
          },
          { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        ],
      },

      // ── Student ──────────────────────────────────────────────
      {
        path: 'student',
        canActivate: [roleGuard(['STUDENT'])],
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/student/student-dashboard/student-dashboard.component').then(m => m.StudentDashboardComponent),
          },
          {
            path: 'timetable',
            loadComponent: () => import('./features/student/timetable/student-timetable.component').then(m => m.StudentTimetableComponent),
          },
          {
            path: 'homework',
            loadComponent: () => import('./features/student/homework/student-homework.component').then(m => m.StudentHomeworkComponent),
          },
          { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        ],
      },

      // ── Parent ────────────────────────────────────────────────
      {
        path: 'parent',
        canActivate: [roleGuard(['PARENT'])],
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/parent/parent-dashboard/parent-dashboard.component').then(m => m.ParentDashboardComponent),
          },
          {
            path: 'homework',
            loadComponent: () => import('./features/parent/homework/parent-homework.component').then(m => m.ParentHomeworkComponent),
          },
          { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        ],
      },
    ],
  },

  { path: '**', redirectTo: 'login' },
];
