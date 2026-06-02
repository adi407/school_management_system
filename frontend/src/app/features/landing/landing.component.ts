import { Component, inject, OnInit, AfterViewInit, NgZone } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'sms-landing',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss'],
})
export class LandingComponent implements OnInit, AfterViewInit {
  private router = inject(Router);
  private auth   = inject(AuthService);
  private zone   = inject(NgZone);

  year = new Date().getFullYear();

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      const role = this.auth.role();
      if (role === 'SCHOOL_ADMIN' || role === 'ACCOUNTANT' || role === 'LIBRARIAN') this.router.navigate(['/admin/dashboard']);
      else if (role === 'TEACHER')    this.router.navigate(['/teacher/dashboard']);
      else if (role === 'STUDENT')    this.router.navigate(['/student/dashboard']);
      else if (role === 'PARENT')     this.router.navigate(['/parent/dashboard']);
      else if (role === 'SUPER_ADMIN') this.router.navigate(['/super-admin/dashboard']);
    }
  }

  ngAfterViewInit(): void {
    this.zone.runOutsideAngular(() => {
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              entry.target.classList.add('is-visible');
            }
          });
        },
        { threshold: 0.15 }
      );
      document.querySelectorAll('.reveal').forEach(el => observer.observe(el));
    });
  }
}
