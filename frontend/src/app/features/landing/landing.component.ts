import { Component, inject, OnInit, AfterViewInit, OnDestroy, NgZone, HostListener } from '@angular/core';
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
export class LandingComponent implements OnInit, AfterViewInit, OnDestroy {
  private router = inject(Router);
  private auth   = inject(AuthService);
  private zone   = inject(NgZone);
  theme          = inject(ThemeService);

  year = new Date().getFullYear();
  mobileMenuOpen = false;
  showBackToTop = false;
  showMobileCta = false;

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
      requestAnimationFrame(() => {
        document.querySelector('.hero')?.classList.add('is-visible');
      });

      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              entry.target.classList.add('is-visible');
            }
          });
        },
        { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
      );
      document.querySelectorAll('.reveal-section:not(.hero)').forEach(el => observer.observe(el));
    });
  }

  @HostListener('window:scroll')
  onScroll(): void {
    const y = window.scrollY;
    this.showBackToTop = y > 600;
    this.showMobileCta = y > 400;
  }

  toggleMobile(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobile(): void {
    this.mobileMenuOpen = false;
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  ngOnDestroy(): void {
    this.mobileMenuOpen = false;
  }
}
