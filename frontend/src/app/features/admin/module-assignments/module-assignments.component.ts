import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModuleAssignmentService } from '../../../core/services/module-assignment.service';
import { StaffService } from '../../../core/services/staff.service';
import { StaffDto } from '../../../core/models/staff.model';
import {
  StaffModule,
  StaffModuleAssignmentDto,
  MODULE_LABELS,
  MODULE_ICONS,
  MODULE_PERMISSIONS,
  formatPermissionLabel,
} from '../../../core/models/module.model';

@Component({
  selector: 'sms-module-assignments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './module-assignments.component.html',
  styleUrls: ['./module-assignments.component.scss'],
})
export class ModuleAssignmentsComponent implements OnInit {
  private svc   = inject(ModuleAssignmentService);
  private staffSvc = inject(StaffService);

  // ── State ──────────────────────────────────────────────────────────────────

  staffList       = signal<StaffDto[]>([]);
  selectedStaff   = signal<StaffDto | null>(null);
  assignments     = signal<StaffModuleAssignmentDto[]>([]);
  expandedModule  = signal<StaffModule | null>(null);   // for sub-permission panel
  loadingStaff    = signal(false);
  loadingModules  = signal(false);
  saving          = signal<StaffModule | null>(null);   // which module is mid-save

  searchQuery = signal('');

  // ── Constants exposed to template ─────────────────────────────────────────

  readonly allModules   = Object.keys(MODULE_LABELS) as StaffModule[];
  readonly moduleLabels = MODULE_LABELS;
  readonly moduleIcons  = MODULE_ICONS;
  readonly modulePerms  = MODULE_PERMISSIONS;

  formatPermLabel = formatPermissionLabel;

  // ── Computed ───────────────────────────────────────────────────────────────

  filteredStaff = computed(() => {
    const q = this.searchQuery().toLowerCase();
    return this.staffList().filter(s =>
      !q ||
      (s.fullName ?? '').toLowerCase().includes(q) ||
      s.email.toLowerCase().includes(q) ||
      s.role.toLowerCase().includes(q)
    );
  });

  /** Quick lookup: module → active assignment (or undefined) */
  activeAssignments = computed(() => {
    const map = new Map<StaffModule, StaffModuleAssignmentDto>();
    this.assignments()
      .filter(a => a.isActive)
      .forEach(a => map.set(a.module, a));
    return map;
  });

  staffDisplayName(s: StaffDto): string {
    return s.fullName?.trim() || s.email;
  }

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
    if (a.subPermissions === null) return true;   // full access → all granted
    return a.subPermissions.includes(perm);
  }

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  ngOnInit() {
    this.loadingStaff.set(true);
    this.staffSvc.list().subscribe({
      next: list => { this.staffList.set(list); this.loadingStaff.set(false); },
      error: () => this.loadingStaff.set(false),
    });
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  selectStaff(staff: StaffDto) {
    this.selectedStaff.set(staff);
    this.expandedModule.set(null);
    this.loadAssignments(staff.id);
  }

  updateSearch(value: string) {
    this.searchQuery.set(value);
  }

  toggleModule(module: StaffModule) {
    const staff = this.selectedStaff();
    if (!staff || this.saving() !== null) return;

    const currently = this.isModuleAssigned(module);

    this.saving.set(module);
    if (currently) {
      // Revoke
      this.svc.revokeModule(staff.id, module).subscribe({
        next: ()  => { this.loadAssignments(staff.id); this.saving.set(null); },
        error: () => this.saving.set(null),
      });
    } else {
      // Assign with full access (null sub-permissions)
      this.svc.assignModule(staff.id, { module, subPermissions: [] }).subscribe({
        next: ()  => { this.loadAssignments(staff.id); this.saving.set(null); },
        error: () => this.saving.set(null),
      });
    }
  }

  toggleExpanded(module: StaffModule) {
    this.expandedModule.update(cur => (cur === module ? null : module));
  }

  toggleSubPermission(module: StaffModule, perm: string) {
    const staff = this.selectedStaff();
    const a = this.getAssignment(module);
    if (!staff || !a || this.saving() !== null) return;

    let current: string[] = a.subPermissions === null
      ? [...(this.modulePerms[module] ?? [])]   // was full-access → start with all
      : [...a.subPermissions];

    if (current.includes(perm)) {
      current = current.filter(p => p !== perm);
    } else {
      current.push(perm);
    }

    this.saving.set(module);
    // If all perms selected, send empty array (= full access / null server-side)
    const allPerms = this.modulePerms[module] ?? [];
    const payload  = current.length === allPerms.length ? [] : current;

    this.svc.assignModule(staff.id, { module, subPermissions: payload }).subscribe({
      next: ()  => { this.loadAssignments(staff.id); this.saving.set(null); },
      error: () => this.saving.set(null),
    });
  }

  grantFullAccess(module: StaffModule) {
    const staff = this.selectedStaff();
    if (!staff || this.saving() !== null) return;
    this.saving.set(module);
    this.svc.assignModule(staff.id, { module, subPermissions: [] }).subscribe({
      next: ()  => { this.loadAssignments(staff.id); this.saving.set(null); },
      error: () => this.saving.set(null),
    });
  }

  // ── Private ────────────────────────────────────────────────────────────────

  private loadAssignments(staffId: string) {
    this.loadingModules.set(true);
    this.svc.getStaffModules(staffId).subscribe({
      next:  list => { this.assignments.set(list); this.loadingModules.set(false); },
      error: ()   => this.loadingModules.set(false),
    });
  }
}
