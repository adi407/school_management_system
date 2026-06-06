import { Component, inject, OnInit, AfterViewInit, NgZone } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';

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
  theme          = inject(ThemeService);

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
      // Hero animates in immediately on load
      requestAnimationFrame(() => {
        document.querySelector('.hero-section')?.classList.add('is-visible');
      });

      // Story sections animate when entering view (low threshold for large 100vh sections)
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              entry.target.classList.add('is-visible');
            }
          });
        },
        { threshold: 0.08, rootMargin: '0px 0px -60px 0px' }
      );
      document.querySelectorAll('.reveal-section:not(.hero-section)').forEach(el => observer.observe(el));
    });
  }
}
