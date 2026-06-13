import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'sms-about',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.scss'],
})
export class AboutComponent {
  theme = inject(ThemeService);
  year = new Date().getFullYear();
}
