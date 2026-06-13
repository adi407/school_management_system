import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'sms-not-found',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.scss'],
})
export class NotFoundComponent {
  theme = inject(ThemeService);
}
