import { Component, inject, OnInit, ElementRef } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { IConsole } from 'app/entities/console/console.model';
import { ConsoleService } from 'app/entities/console/service/console.service';
import { GameService } from '../service/game.service';
import { IGame } from '../game.model';
import { GameFormService, GameFormGroup } from './game-form.service';

@Component({
  standalone: true,
  selector: 'jhi-game-update',
  templateUrl: './game-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class GameUpdateComponent implements OnInit {
  isSaving = false;
  game: IGame | null = null;

  consolesSharedCollection: IConsole[] = [];

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected gameService = inject(GameService);
  protected gameFormService = inject(GameFormService);
  protected consoleService = inject(ConsoleService);
  protected elementRef = inject(ElementRef);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: GameFormGroup = this.gameFormService.createGameFormGroup();

  compareConsole = (o1: IConsole | null, o2: IConsole | null): boolean => this.consoleService.compareConsole(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ game }) => {
      this.game = game;
      if (game) {
        this.updateForm(game);
      }

      this.loadRelationshipsOptions();
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
    const game = this.gameFormService.getGame(this.editForm);
    if (game.id !== null) {
      this.subscribeToSaveResponse(this.gameService.update(game));
    } else {
      this.subscribeToSaveResponse(this.gameService.create(game));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IGame>>): void {
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

  protected updateForm(game: IGame): void {
    this.game = game;
    this.gameFormService.resetForm(this.editForm, game);

    this.consolesSharedCollection = this.consoleService.addConsoleToCollectionIfMissing<IConsole>(
      this.consolesSharedCollection,
      game.console,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.consoleService
      .query()
      .pipe(map((res: HttpResponse<IConsole[]>) => res.body ?? []))
      .pipe(map((consoles: IConsole[]) => this.consoleService.addConsoleToCollectionIfMissing<IConsole>(consoles, this.game?.console)))
      .subscribe((consoles: IConsole[]) => (this.consolesSharedCollection = consoles));
  }
}
