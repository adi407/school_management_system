import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'sms-pricing',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './pricing.component.html',
  styleUrls: ['./pricing.component.scss'],
})
export class PricingComponent {
  theme = inject(ThemeService);
  year = new Date().getFullYear();
}
