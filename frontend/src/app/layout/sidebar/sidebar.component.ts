import { Component, Input, Output, EventEmitter, computed, inject } from '@angular/core';
import { RouterModule, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Role } from '../../core/models/user.model';
import { StaffModule } from '../../core/models/module.model';

interface NavItem {
  label:   string;
  route:   string;
  icon:    string;
  /** If set, item is only visible when the user holds this module */
  module?: StaffModule;
  /** If set, item is only visible for these roles (used for non-admin roles) */
  roles?:  Role[];
}

interface NavGroup {
  label:   string;
  items:   NavItem[];
  /**
   * For SCHOOL_ADMIN: group is visible if ANY item inside it is visible.
   * For other roles: group is visible if user's role is in this list.
   */
  roles?:  Role[];
}

@Component({
  selector: 'sms-sidebar',
  standalone: true,
  imports: [RouterModule, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent {
  @Input() collapsed  = false;
  @Input() mobileOpen = false;
  @Output() toggleCollapse = new EventEmitter<void>();
  @Output() closeMobile    = new EventEmitter<void>();

  private auth = inject(AuthService);
  user = this.auth.user;

  private readonly allGroups: NavGroup[] = [
    // ── Super Admin ───────────────────────────────────────────────────────────
    {
      label: 'Platform',
      roles: ['SUPER_ADMIN'],
      items: [
        { label: 'Dashboard', route: '/super-admin/dashboard', icon: '◈' },
        { label: 'Schools',   route: '/super-admin/schools',   icon: '🏫' },
      ],
    },

    // ── Admin: always visible (no module gate) ────────────────────────────────
    {
      label: 'Overview',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Dashboard', route: '/admin/dashboard', icon: '◈' },
      ],
    },

    // ── Admin: SCHOOL_SETUP module ────────────────────────────────────────────
    {
      label: 'Setup',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Academic Years',     route: '/admin/academic-years',     icon: '🗓',  module: 'SCHOOL_SETUP' },
        { label: 'Subjects',           route: '/admin/subjects',           icon: '📚',  module: 'SCHOOL_SETUP' },
        { label: 'Classes',            route: '/admin/classes',            icon: '🏫',  module: 'SCHOOL_SETUP' },
        { label: 'Module Assignments', route: '/admin/module-assignments', icon: '⚙️', module: 'SCHOOL_SETUP' },
      ],
    },

    // ── Admin: TEACHING + EXAMINATIONS modules ────────────────────────────────
    {
      label: 'Academic',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Students',   route: '/admin/students',   icon: '👥', module: 'TEACHING' },
        { label: 'Attendance', route: '/admin/attendance', icon: '✓',  module: 'TEACHING' },
        { label: 'Homework',   route: '/admin/homework',   icon: '📝', module: 'TEACHING' },
        { label: 'Timetable',  route: '/admin/timetable',  icon: '📅', module: 'TEACHING' },
        { label: 'Exams',      route: '/admin/exams',      icon: '📋', module: 'EXAMINATIONS' },
      ],
    },

    // ── Admin: WELLNESS module ────────────────────────────────────────────────
    {
      label: 'Wellness ✨',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Campus Pulse', route: '/admin/pulse', icon: '💙', module: 'WELLNESS' },
      ],
    },

    // ── Admin: ACCOUNTING + PAYROLL modules ───────────────────────────────────
    {
      label: 'Finance',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Fees',       route: '/admin/fees',       icon: '💰', module: 'ACCOUNTING' },
        { label: 'Payroll',    route: '/admin/payroll',    icon: '💼', module: 'PAYROLL'    },
        { label: 'P&L Report', route: '/admin/finance/pl', icon: '📊', module: 'ACCOUNTING' },
      ],
    },

    // ── Admin: AI-Powered Features ──────────────────────────────────────────
    {
      label: 'AI Assistant',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Smart Substitute', route: '/admin/smart-substitute', icon: '🔄' },
        { label: 'PTM Prep',         route: '/admin/ptm-prep',         icon: '🤝' },
      ],
    },

    // ── Admin: HR module ──────────────────────────────────────────────────────
    {
      label: 'HR & Staff',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Staff',            route: '/admin/staff',            icon: '👨‍🏫', module: 'HR' },
        { label: 'Staff Attendance', route: '/admin/staff-attendance', icon: '✓',   module: 'HR' },
      ],
    },

    // ── Admin: ANNOUNCEMENTS module ───────────────────────────────────────────
    {
      label: 'Communication',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Announcements', route: '/admin/announcements', icon: '📢', module: 'ANNOUNCEMENTS' },
      ],
    },

    // ── Admin: LIBRARY module ─────────────────────────────────────────────────
    {
      label: 'Library',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Library', route: '/admin/library', icon: '📚', module: 'LIBRARY' },
      ],
    },

    // ── Admin: ADMISSIONS module ──────────────────────────────────────────────
    {
      label: 'Admissions',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Students', route: '/admin/students', icon: '🎓', module: 'ADMISSIONS' },
      ],
    },

    // ── Admin: no module gate (activity management) ───────────────────────────
    {
      label: 'Co-Curricular',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Activities', route: '/admin/activities', icon: '🏆' },
      ],
    },

    // ── Teacher ───────────────────────────────────────────────────────────────
    {
      label: 'Overview',
      roles: ['TEACHER'],
      items: [
        { label: 'Dashboard', route: '/teacher/dashboard', icon: '◈' },
      ],
    },
    {
      label: 'Teaching',
      roles: ['TEACHER'],
      items: [
        { label: 'Homework',      route: '/teacher/homework',      icon: '📝' },
        { label: 'Attendance',    route: '/teacher/attendance',    icon: '✓'  },
        { label: 'Announcements', route: '/teacher/announcements', icon: '📢' },
        { label: 'Campus Pulse',  route: '/teacher/pulse',         icon: '💙' }, // must be /teacher/* not /admin/*
      ],
    },

    // ── Student ───────────────────────────────────────────────────────────────
    {
      label: 'Overview',
      roles: ['STUDENT'],
      items: [
        { label: 'Dashboard', route: '/student/dashboard', icon: '◈' },
      ],
    },
    {
      label: 'My Studies',
      roles: ['STUDENT'],
      items: [
        { label: 'Timetable', route: '/student/timetable', icon: '📅' },
        { label: 'Homework',  route: '/student/homework',  icon: '📝' },
      ],
    },

    // ── Parent ────────────────────────────────────────────────────────────────
    {
      label: 'Overview',
      roles: ['PARENT'],
      items: [
        { label: 'Dashboard', route: '/parent/dashboard', icon: '◈' },
      ],
    },
    {
      label: 'My Child',
      roles: ['PARENT'],
      items: [
        { label: 'Homework', route: '/parent/homework', icon: '📝' },
      ],
    },
  ];

  visibleGroups = computed(() => {
    const role   = this.auth.role();
    if (!role) return [];

    return this.allGroups
      .filter(g => !g.roles || g.roles.includes(role))
      .map(g => ({
        ...g,
        items: g.items.filter(item => this.isItemVisible(item, role)),
      }))
      .filter(g => g.items.length > 0);
  });

  private isItemVisible(item: NavItem, role: Role): boolean {
    // Items without a module gate are always visible (for the current role group)
    if (!item.module) return true;

    // SUPER_ADMIN sees everything
    if (role === 'SUPER_ADMIN') return true;

    // For SCHOOL_ADMIN: check module assignment
    if (role === 'SCHOOL_ADMIN') return this.auth.hasModule(item.module);

    // For other roles: no module filtering (they rely on group-level role filter)
    return true;
  }

  logout() { this.auth.logout(); }
}
