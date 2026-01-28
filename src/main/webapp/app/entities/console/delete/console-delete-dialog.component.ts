import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IConsole } from '../console.model';
import { ConsoleService } from '../service/console.service';

@Component({
  standalone: true,
  templateUrl: './console-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ConsoleDeleteDialogComponent {
  console?: IConsole;

  protected consoleService = inject(ConsoleService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.consoleService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
