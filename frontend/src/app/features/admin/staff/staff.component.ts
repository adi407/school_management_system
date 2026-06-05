import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DatePipe } from '@angular/common';
import { StaffService } from '../../../core/services/staff.service';
import { ToastService } from '../../../core/services/toast.service';
import { StaffDto, STAFF_ROLES } from '../../../core/models/staff.model';

@Component({
  selector: 'sms-staff',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, RouterModule, DatePipe],
  templateUrl: './staff.component.html',
  styleUrls: ['./staff.component.scss'],
})
export class StaffComponent implements OnInit {
  search      = signal('');
  roleFilter  = signal('');
  loading     = signal(false);
  saving      = signal(false);
  showModal   = signal(false);
  editingId   = signal<string | null>(null);

  staff = signal<StaffDto[]>([]);

  readonly staffRoles = STAFF_ROLES;

  departments = computed(() => [...new Set(this.staff().map(s => s.department).filter(Boolean))]);

  filtered = computed(() => {
    const q = this.search().toLowerCase();
    const r = this.roleFilter();
    return this.staff().filter(s =>
      (!q || s.fullName.toLowerCase().includes(q) || s.email.toLowerCase().includes(q)) &&
      (!r || s.role === r)
    );
  });

  activeCount = computed(() => this.staff().filter(s => s.isActive).length);

  form = this.fb.group({
    firstName:  ['', Validators.required],
    lastName:   ['', Validators.required],
    email:      ['', [Validators.required, Validators.email]],
    phone:      [''],
    department: [''],
    role:       ['TEACHER', Validators.required],
    password:   [''],
  });

  constructor(
    private fb: FormBuilder,
    private staffService: StaffService,
    private toast: ToastService,
  ) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.staffService.list().subscribe({
      next: s => { this.staff.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openCreate() {
    this.editingId.set(null);
    this.form.reset({ role: 'TEACHER' });
    this.form.get('email')!.enable();
    this.showModal.set(true);
  }

  openEdit(member: StaffDto) {
    this.editingId.set(member.id);
    this.form.patchValue({
      firstName: member.firstName ?? '',
      lastName:  member.lastName ?? '',
      email:     member.email,
      phone:     member.phone ?? '',
      department: member.department ?? '',
      role:      member.role,
    });
    this.form.get('email')!.disable();
    this.showModal.set(true);
  }

  closeModal() { this.showModal.set(false); }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    this.saving.set(true);
    const editing = this.editingId();

    if (editing) {
      this.staffService.update(editing, {
        firstName:  v.firstName!,
        lastName:   v.lastName!,
        phone:      v.phone || undefined,
        department: v.department || undefined,
        role:       v.role!,
      }).subscribe({
        next: updated => {
          this.staff.update(list => list.map(s => s.id === editing ? updated : s));
          this.saving.set(false); this.closeModal();
          this.toast.success('Staff member updated');
        },
        error: () => { this.saving.set(false); this.toast.error('Update failed'); },
      });
    } else {
      this.staffService.create({
        email:      v.email!,
        firstName:  v.firstName!,
        lastName:   v.lastName!,
        phone:      v.phone || null,
        department: v.department || null,
        role:       v.role!,
        password:   v.password || null,
      }).subscribe({
        next: created => {
          this.staff.update(list => [created, ...list]);
          this.saving.set(false); this.closeModal();
          this.toast.success('Staff member created — default password: Welcome@1234');
        },
        error: (err) => {
          this.saving.set(false);
          this.toast.error(err?.error?.message ?? 'Failed to create staff');
        },
      });
    }
  }

  deactivate(member: StaffDto) {
    if (!confirm(`Deactivate ${member.fullName}?`)) return;
    this.staffService.deactivate(member.id).subscribe({
      next: () => {
        this.staff.update(list => list.map(s => s.id === member.id ? { ...s, isActive: false } : s));
        this.toast.success(`${member.fullName} deactivated`);
      },
      error: () => this.toast.error('Deactivation failed'),
    });
  }

  displayName(member: StaffDto): string {
    const name = member.fullName?.trim();
    if (name && name !== member.email && !name.includes('@')) return name;
    // Fall back to the part before @ in email
    return member.email.split('@')[0].replace(/[._]/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
  }

  initials(name: string) {
    const display = name?.includes('@') ? name.split('@')[0] : name;
    return (display ?? '?').split(/[\s._]+/).map(p => p[0] ?? '').join('').toUpperCase().slice(0, 2);
  }

  roleColor(r: string) {
    const m: Record<string, string> = {
      TEACHER: 'blue', ACCOUNTANT: 'green', LIBRARIAN: 'purple',
      TRANSPORT_MANAGER: 'orange', HOSTEL_WARDEN: 'red',
    };
    return m[r] ?? 'gray';
  }

  get f() { return this.form.controls; }
}
