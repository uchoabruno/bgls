import { Component, inject, OnInit, ElementRef } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { ConsoleService } from '../service/console.service';
import { IConsole } from '../console.model';
import { ConsoleFormService, ConsoleFormGroup } from './console-form.service';

@Component({
  standalone: true,
  selector: 'jhi-console-update',
  templateUrl: './console-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ConsoleUpdateComponent implements OnInit {
  isSaving = false;
  console: IConsole | null = null;

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected consoleService = inject(ConsoleService);
  protected consoleFormService = inject(ConsoleFormService);
  protected elementRef = inject(ElementRef);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ConsoleFormGroup = this.consoleFormService.createConsoleFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ console }) => {
      this.console = console;
      if (console) {
        this.updateForm(console);
      }
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(new EventWithContent<AlertError>('bglsApp.error', { ...err, key: 'error.file.' + err.key })),
    });
  }

  clearInputImage(field: string, fieldContentType: string, idInput: string): void {
    this.editForm.patchValue({
      [field]: null,
      [fieldContentType]: null,
    });
    if (idInput && this.elementRef.nativeElement.querySelector('#' + idInput)) {
      this.elementRef.nativeElement.querySelector('#' + idInput).value = null;
    }
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const console = this.consoleFormService.getConsole(this.editForm);
    if (console.id !== null) {
      this.subscribeToSaveResponse(this.consoleService.update(console));
    } else {
      this.subscribeToSaveResponse(this.consoleService.create(console));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IConsole>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(console: IConsole): void {
    this.console = console;
    this.consoleFormService.resetForm(this.editForm, console);
  }
}
