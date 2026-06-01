import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivityService, ActivityDto, CreateActivityRequest } from '../../../core/services/activity.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-activities',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './activities.component.html',
  styleUrls: ['./activities.component.scss'],
})
export class ActivitiesComponent implements OnInit {
  private actSvc = inject(ActivityService);
  private toast  = inject(ToastService);

  loading    = signal(true);
  saving     = signal(false);
  activities = signal<ActivityDto[]>([]);
  showModal  = signal(false);
  editingId  = signal<string | null>(null);

  categories = ['SPORTS', 'ACADEMIC', 'MUSIC', 'ARTS', 'CULTURAL', 'OTHER'];

  form = signal<CreateActivityRequest>({
    name: '', category: 'SPORTS', coach: '', schedule: '', capacity: 30, status: 'ACTIVE',
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.actSvc.list().subscribe({
      next: d => { this.activities.set(d); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load activities'); },
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.set({ name: '', category: 'SPORTS', coach: '', schedule: '', capacity: 30, status: 'ACTIVE' });
    this.showModal.set(true);
  }

  openEdit(act: ActivityDto): void {
    this.editingId.set(act.id);
    this.form.set({
      name: act.name, category: act.category,
      coach: act.coach ?? '', schedule: act.schedule ?? '',
      capacity: act.capacity, status: act.status,
    });
    this.showModal.set(true);
  }

  save(): void {
    const f = this.form();
    if (!f.name) { this.toast.warning('Activity name is required'); return; }
    this.saving.set(true);
    const id = this.editingId();
    const obs = id ? this.actSvc.update(id, f) : this.actSvc.create(f);
    obs.subscribe({
      next: () => {
        this.toast.success(id ? 'Activity updated' : 'Activity created');
        this.showModal.set(false);
        this.saving.set(false);
        this.load();
      },
      error: () => { this.toast.error('Failed to save activity'); this.saving.set(false); },
    });
  }

  delete(id: string): void {
    if (!confirm('Delete this activity?')) return;
    this.actSvc.delete(id).subscribe({
      next: () => { this.toast.success('Activity deleted'); this.load(); },
      error: () => this.toast.error('Failed to delete activity'),
    });
  }

  updateForm(patch: Partial<CreateActivityRequest>): void {
    this.form.update(f => ({ ...f, ...patch }));
  }

  categoryColor(c: string) {
    const m: Record<string, string> = {
      SPORTS: 'green', ACADEMIC: 'blue', MUSIC: 'purple',
      ARTS: 'orange', CULTURAL: 'yellow', OTHER: 'gray',
    };
    return m[c] ?? 'gray';
  }
}
