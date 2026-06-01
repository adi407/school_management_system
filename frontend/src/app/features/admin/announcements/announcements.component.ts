import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { AnnouncementService } from '../../../core/services/announcement.service';
import { ToastService } from '../../../core/services/toast.service';
import { AnnouncementDto } from '../../../core/models/announcement.model';

const ALL_ROLES = ['SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT', 'ACCOUNTANT'];

@Component({
  selector: 'sms-announcements',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, DatePipe],
  templateUrl: './announcements.component.html',
  styleUrls: ['./announcements.component.scss'],
})
export class AnnouncementsComponent implements OnInit {
  loading   = signal(false);
  saving    = signal(false);
  showModal = signal(false);
  editingId = signal<string | null>(null);

  items   = signal<AnnouncementDto[]>([]);
  search  = signal('');

  readonly allRoles = ALL_ROLES;

  filtered = computed(() => {
    const q = this.search().toLowerCase();
    return q
      ? this.items().filter(a => a.title.toLowerCase().includes(q) || a.body.toLowerCase().includes(q))
      : this.items();
  });

  pinnedCount = computed(() => this.items().filter(a => a.isPinned).length);

  form = this.fb.group({
    title:       ['', Validators.required],
    body:        ['', Validators.required],
    targetRoles: [[] as string[]],
    isPinned:    [false],
    expiresAt:   [null as string | null],
  });

  constructor(
    private fb: FormBuilder,
    private announcementService: AnnouncementService,
    private toast: ToastService,
  ) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.announcementService.listAll().subscribe({
      next: items => { this.items.set(items); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openCreate() {
    this.editingId.set(null);
    this.form.reset({ targetRoles: [], isPinned: false });
    this.showModal.set(true);
  }

  openEdit(item: AnnouncementDto) {
    this.editingId.set(item.id);
    this.form.patchValue({
      title:       item.title,
      body:        item.body,
      targetRoles: item.targetRoles ?? [],
      isPinned:    item.isPinned,
      expiresAt:   item.expiresAt ? new Date(item.expiresAt).toISOString().substring(0, 16) : null,
    });
    this.showModal.set(true);
  }

  closeModal() { this.showModal.set(false); }

  toggleRole(role: string) {
    const current = this.form.value.targetRoles as string[] ?? [];
    const updated = current.includes(role)
      ? current.filter(r => r !== role)
      : [...current, role];
    this.form.patchValue({ targetRoles: updated });
  }

  isRoleSelected(role: string) {
    return (this.form.value.targetRoles as string[] ?? []).includes(role);
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.value;
    const req = {
      title:       v.title!,
      body:        v.body!,
      targetRoles: v.targetRoles as string[] ?? [],
      isPinned:    v.isPinned ?? false,
      expiresAt:   v.expiresAt ? new Date(v.expiresAt).toISOString() : null,
    };
    this.saving.set(true);
    const editing = this.editingId();

    const call = editing
      ? this.announcementService.update(editing, req)
      : this.announcementService.create(req);

    call.subscribe({
      next: result => {
        if (editing) {
          this.items.update(list => list.map(a => a.id === editing ? result : a));
        } else {
          this.items.update(list => [result, ...list]);
        }
        this.saving.set(false); this.closeModal();
        this.toast.success(editing ? 'Announcement updated' : 'Announcement published');
      },
      error: () => { this.saving.set(false); this.toast.error('Failed to save announcement'); },
    });
  }

  delete(item: AnnouncementDto) {
    if (!confirm('Delete this announcement?')) return;
    this.announcementService.delete(item.id).subscribe({
      next: () => {
        this.items.update(list => list.filter(a => a.id !== item.id));
        this.toast.success('Announcement deleted');
      },
      error: () => this.toast.error('Delete failed'),
    });
  }

  isExpired(item: AnnouncementDto) {
    return item.expiresAt && new Date(item.expiresAt) < new Date();
  }

  rolesLabel(roles: string[]) {
    if (!roles?.length) return 'Everyone';
    return roles.map(r => r.charAt(0) + r.slice(1).toLowerCase()).join(', ');
  }

  get f() { return this.form.controls; }
}
