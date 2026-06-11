import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ModuleAssignmentService } from '../../../../core/services/module-assignment.service';
import { SchoolService } from '../../../../core/services/school.service';
import { ToastService } from '../../../../core/services/toast.service';
import {
  SchoolUserDto,
  StaffModule,
  StaffModuleAssignmentDto,
  MODULE_LABELS,
  MODULE_ICONS,
  MODULE_PERMISSIONS,
  formatPermissionLabel,
} from '../../../../core/models/module.model';
import { SchoolDto } from '../../../../core/models/school.model';

@Component({
  selector: 'sms-sa-admin-modules',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './sa-admin-modules.component.html',
  styleUrls: ['./sa-admin-modules.component.scss'],
})
export class SaAdminModulesComponent implements OnInit {
  private route      = inject(ActivatedRoute);
  private moduleSvc  = inject(ModuleAssignmentService);
  private schoolSvc  = inject(SchoolService);
  private toast      = inject(ToastService);

  readonly schoolId = this.route.snapshot.paramMap.get('id')!;

  // ── State ──────────────────────────────────────────────────────────────────
  school        = signal<SchoolDto | null>(null);
  users         = signal<SchoolUserDto[]>([]);
  selectedUser  = signal<SchoolUserDto | null>(null);
  assignments   = signal<StaffModuleAssignmentDto[]>([]);
  expandedMod   = signal<StaffModule | null>(null);
  loadingUsers  = signal(true);
  loadingMods   = signal(false);
  saving        = signal<StaffModule | null>(null);
  grantingAll   = signal(false);
  searchQuery   = signal('');

  // ── Reset password modal ───────────────────────────────────────────────────
  showResetModal    = signal(false);
  resetTarget       = signal<SchoolUserDto | null>(null);
  resetSaving       = signal(false);
  resetError        = signal('');
  newPassword       = '';
  confirmPassword   = '';
  showNewPass       = signal(false);
  showConfirmPass   = signal(false);

  // ── Constants ──────────────────────────────────────────────────────────────
  readonly allModules    = Object.keys(MODULE_LABELS) as StaffModule[];
  readonly moduleLabels  = MODULE_LABELS;
  readonly moduleIcons   = MODULE_ICONS;
  readonly modulePerms   = MODULE_PERMISSIONS;
  readonly formatPermLabel = formatPermissionLabel;

  // ── Computed ───────────────────────────────────────────────────────────────
  filteredUsers = computed(() => {
    const q = this.searchQuery().toLowerCase();
    return this.users().filter(u =>
      !q ||
      u.fullName.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      u.role.toLowerCase().includes(q)
    );
  });

  activeAssignments = computed(() => {
    const map = new Map<StaffModule, StaffModuleAssignmentDto>();
    this.assignments()
      .filter(a => a.isActive)
      .forEach(a => map.set(a.module, a));
    return map;
  });

  assignedCount = computed(() => this.activeAssignments().size);

  isModuleAssigned(m: StaffModule): boolean {
    return this.activeAssignments().has(m);
  }

  getAssignment(m: StaffModule): StaffModuleAssignmentDto | undefined {
    return this.activeAssignments().get(m);
  }

  isFullAccess(m: StaffModule): boolean {
    const a = this.getAssignment(m);
    return !!a && (a.subPermissions === null || a.subPermissions.length === 0);
  }

  subPermissionsFor(m: StaffModule): string[] {
    return this.modulePerms[m] ?? [];
  }

  isPermissionGranted(m: StaffModule, perm: string): boolean {
    const a = this.getAssignment(m);
    if (!a) return false;
    if (a.subPermissions === null) return true;
    return a.subPermissions.includes(perm);
  }

  roleLabel(role: string): string {
    return role.replace(/_/g, ' ');
  }

  roleBadgeClass(role: string): string {
    const map: Record<string, string> = {
      SCHOOL_ADMIN:      'badge--purple',
      TEACHER:           'badge--blue',
      ACCOUNTANT:        'badge--green',
      LIBRARIAN:         'badge--yellow',
      TRANSPORT_MANAGER: 'badge--orange',
      HOSTEL_WARDEN:     'badge--red',
    };
    return map[role] ?? 'badge--grey';
  }

  // ── Lifecycle ──────────────────────────────────────────────────────────────
  ngOnInit() {
    this.schoolSvc.getSchool(this.schoolId).subscribe(s => this.school.set(s));
    this.loadUsers();
  }

  private loadUsers() {
    this.loadingUsers.set(true);
    this.moduleSvc.getSchoolUsers(this.schoolId).subscribe({
      next: list => { this.users.set(list); this.loadingUsers.set(false); },
      error: ()   => { this.loadingUsers.set(false); this.toast.error('Failed to load users'); },
    });
  }

  // ── Actions ────────────────────────────────────────────────────────────────
  selectUser(user: SchoolUserDto) {
    this.selectedUser.set(user);
    this.expandedMod.set(null);
    this.loadAssignments(user.id);
  }

  updateSearch(value: string) {
    this.searchQuery.set(value);
  }

  toggleModule(module: StaffModule) {
    const user = this.selectedUser();
    if (!user || this.saving() !== null) return;

    const currently = this.isModuleAssigned(module);
    this.saving.set(module);

    const done = () => { this.loadAssignments(user.id); this.saving.set(null); };
    const fail = () => { this.saving.set(null); this.toast.error('Failed to update module'); };

    if (currently) {
      this.moduleSvc.saRevokeModule(this.schoolId, user.id, module).subscribe({ next: done, error: fail });
    } else {
      this.moduleSvc.saAssignModule(this.schoolId, user.id, { module, subPermissions: [] }).subscribe({ next: done, error: fail });
    }
  }

  toggleExpanded(module: StaffModule) {
    this.expandedMod.update(cur => (cur === module ? null : module));
  }

  toggleSubPermission(module: StaffModule, perm: string) {
    const user = this.selectedUser();
    const a = this.getAssignment(module);
    if (!user || !a || this.saving() !== null) return;

    let current: string[] = a.subPermissions === null
      ? [...(this.modulePerms[module] ?? [])]
      : [...a.subPermissions];

    current = current.includes(perm)
      ? current.filter(p => p !== perm)
      : [...current, perm];

    this.saving.set(module);
    const allPerms = this.modulePerms[module] ?? [];
    const payload  = current.length === allPerms.length ? [] : current;

    this.moduleSvc.saAssignModule(this.schoolId, user.id, { module, subPermissions: payload })
      .subscribe({
        next: ()  => { this.loadAssignments(user.id); this.saving.set(null); },
        error: () => { this.saving.set(null); this.toast.error('Failed to update permission'); },
      });
  }

  grantFullAccess(module: StaffModule) {
    const user = this.selectedUser();
    if (!user || this.saving() !== null) return;
    this.saving.set(module);
    this.moduleSvc.saAssignModule(this.schoolId, user.id, { module, subPermissions: [] })
      .subscribe({
        next: ()  => { this.loadAssignments(user.id); this.saving.set(null); },
        error: () => { this.saving.set(null); this.toast.error('Failed to grant full access'); },
      });
  }

  grantAllModules() {
    const user = this.selectedUser();
    if (!user || this.grantingAll()) return;
    this.grantingAll.set(true);
    this.moduleSvc.saGrantAllModules(this.schoolId, user.id).subscribe({
      next: () => {
        this.loadAssignments(user.id);
        this.grantingAll.set(false);
        this.toast.success('All modules granted with full access');
      },
      error: () => {
        this.grantingAll.set(false);
        this.toast.error('Failed to grant all modules');
      },
    });
  }

  // ── Reset password ─────────────────────────────────────────────────────────
  openResetPassword(user: SchoolUserDto) {
    this.resetTarget.set(user);
    this.newPassword     = '';
    this.confirmPassword = '';
    this.resetError.set('');
    this.showNewPass.set(false);
    this.showConfirmPass.set(false);
    this.showResetModal.set(true);
  }

  closeResetModal() {
    this.showResetModal.set(false);
    this.resetTarget.set(null);
  }

  submitResetPassword() {
    const user = this.resetTarget();
    if (!user) return;

    // Client-side validation
    if (this.newPassword.length < 8) {
      this.resetError.set('Password must be at least 8 characters.');
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.resetError.set('Passwords do not match.');
      return;
    }

    this.resetError.set('');
    this.resetSaving.set(true);

    this.moduleSvc.saResetPassword(this.schoolId, user.id, this.newPassword).subscribe({
      next: () => {
        this.resetSaving.set(false);
        this.closeResetModal();
        this.toast.success(`Password updated for ${user.fullName}`);
      },
      error: err => {
        this.resetSaving.set(false);
        this.resetError.set(err?.error?.message ?? 'Failed to reset password. Try again.');
      },
    });
  }

  // ── Private ────────────────────────────────────────────────────────────────
  private loadAssignments(userId: string) {
    this.loadingMods.set(true);
    this.moduleSvc.getSaUserModules(this.schoolId, userId).subscribe({
      next: list => { this.assignments.set(list); this.loadingMods.set(false); },
      error: ()  => { this.loadingMods.set(false); this.toast.error('Failed to load modules'); },
    });
  }
}
