import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'sms-privacy',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './privacy.component.html',
  styleUrls: ['../terms/legal.component.scss'],
})
export class PrivacyComponent {
  theme = inject(ThemeService);
  year = new Date().getFullYear();
}
