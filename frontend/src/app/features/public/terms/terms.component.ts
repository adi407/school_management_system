import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'sms-terms',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './terms.component.html',
  styleUrls: ['./legal.component.scss'],
})
export class TermsComponent {
  theme = inject(ThemeService);
  year = new Date().getFullYear();
}
