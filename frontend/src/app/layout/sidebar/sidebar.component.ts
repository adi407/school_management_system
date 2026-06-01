import { Component, Input, Output, EventEmitter, computed, inject } from '@angular/core';
import { RouterModule, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Role } from '../../core/models/user.model';

interface NavItem { label: string; route: string; icon: string; roles?: Role[]; }
interface NavGroup { label: string; items: NavItem[]; roles?: Role[]; }

@Component({
  selector: 'sms-sidebar',
  standalone: true,
  imports: [RouterModule, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Input() mobileOpen = false;
  @Output() toggleCollapse = new EventEmitter<void>();
  @Output() closeMobile    = new EventEmitter<void>();

  private auth = inject(AuthService);
  user = this.auth.user;

  private readonly allGroups: NavGroup[] = [
    {
      label: 'Platform',
      roles: ['SUPER_ADMIN'],
      items: [
        { label: 'Dashboard',  route: '/super-admin/dashboard', icon: '◈' },
        { label: 'Schools',    route: '/super-admin/schools',   icon: '🏫' },
      ],
    },
    {
      label: 'Overview',
      roles: ['SCHOOL_ADMIN', 'ACCOUNTANT', 'LIBRARIAN'],
      items: [
        { label: 'Dashboard', route: '/admin/dashboard', icon: '◈' },
      ],
    },
    {
      label: 'Academic',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Students',   route: '/admin/students',   icon: '👥' },
        { label: 'Attendance', route: '/admin/attendance', icon: '✓' },
        { label: 'Homework',   route: '/admin/homework',   icon: '📝' },
        { label: 'Timetable',  route: '/admin/timetable',  icon: '📅' },
        { label: 'Exams',      route: '/admin/exams',      icon: '📋' },
      ],
    },
    {
      label: 'Wellness ✨',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Campus Pulse', route: '/admin/pulse', icon: '💙' },
      ],
    },
    {
      label: 'Finance',
      roles: ['SCHOOL_ADMIN', 'ACCOUNTANT'],
      items: [
        { label: 'Fees', route: '/admin/fees', icon: '💰' },
      ],
    },
    {
      label: 'HR & Staff',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Staff', route: '/admin/staff', icon: '👨‍🏫' },
      ],
    },
    {
      label: 'Communication',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Announcements', route: '/admin/announcements', icon: '📢' },
      ],
    },
    {
      label: 'Library',
      roles: ['SCHOOL_ADMIN', 'LIBRARIAN'],
      items: [
        { label: 'Library', route: '/admin/library', icon: '📚' },
      ],
    },
    {
      label: 'Co-Curricular',
      roles: ['SCHOOL_ADMIN'],
      items: [
        { label: 'Activities', route: '/admin/activities', icon: '🏆' },
      ],
    },
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
        { label: 'Homework',       route: '/teacher/homework',       icon: '📝' },
        { label: 'Attendance',     route: '/teacher/attendance',     icon: '✓'  },
        { label: 'Announcements',  route: '/teacher/announcements',  icon: '📢' },
        { label: 'Campus Pulse',   route: '/admin/pulse',            icon: '💙' },
      ],
    },
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
    const role = this.auth.role();
    if (!role) return [];
    return this.allGroups.filter(g => !g.roles || g.roles.includes(role));
  });

  logout() { this.auth.logout(); }
}
