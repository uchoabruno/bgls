import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, Observable, of, switchMap, take } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IGame } from 'app/entities/game/game.model';
import { GameService } from 'app/entities/game/service/game.service';
import { ItemService } from '../service/item.service';
import { IItem } from '../item.model';
import { ItemFormGroup, ItemFormService } from './item-form.service';
import { AccountService } from '../../../core/auth/account.service';

@Component({
  standalone: true,
  selector: 'jhi-item-update',
  templateUrl: './item-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ItemUpdateComponent implements OnInit {
  isSaving = false;
  item: IItem | null = null;

  usersSharedCollection: IUser[] = [];
  gamesSharedCollection: IGame[] = [];

  protected accountService = inject(AccountService);
  protected itemService = inject(ItemService);
  protected itemFormService = inject(ItemFormService);
  protected userService = inject(UserService);
  protected gameService = inject(GameService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ItemFormGroup = this.itemFormService.createItemFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);
  compareGame = (o1: IGame | null, o2: IGame | null): boolean => this.gameService.compareGame(o1, o2);

  ngOnInit(): void {
    forkJoin([
      this.activatedRoute.data.pipe(
        take(1),
        map(data => data.item as IItem | null),
      ),
      this.activatedRoute.queryParamMap.pipe(
        take(1),
        map(params => params.get('gameId')),
      ),
      this.accountService.identity().pipe(take(1)),
    ])
      .pipe(
        switchMap(([resolvedItem, gameIdFromQuery, currentUser]) => {
          this.item = resolvedItem;

          if (!this.item && currentUser?.login) {
            return this.userService.query({ 'login.equals': currentUser.login }).pipe(
              map((res: HttpResponse<IUser[]>) => res.body?.[0] ?? null),
              map(ownerUser => ({ resolvedItem, gameIdFromQuery, currentUser, ownerUser })),
            );
          } else {
            return of({ resolvedItem, gameIdFromQuery, currentUser, ownerUser: null });
          }
        }),
        // eslint-disable-next-line arrow-body-style
        switchMap(({ resolvedItem, gameIdFromQuery, currentUser, ownerUser }) => {
          return this.loadRelationshipsOptionsObservables(resolvedItem, ownerUser ?? currentUser).pipe(
            map(([usersCollection, gamesCollection]) => ({
              resolvedItem,
              gameIdFromQuery,
              currentUser,
              ownerUser,
              usersCollection,
              gamesCollection,
            })),
          );
        }),
        map(({ resolvedItem, gameIdFromQuery, currentUser, ownerUser, usersCollection, gamesCollection }) => {
          this.usersSharedCollection = usersCollection;
          this.gamesSharedCollection = gamesCollection;

          if (resolvedItem) {
            this.updateForm(resolvedItem);
          } else {
            if (ownerUser && !this.editForm.get('owner')?.value) {
              this.editForm.get('owner')?.setValue(ownerUser);
            }
          }

          if (gameIdFromQuery && this.item?.game == null) {
            const gameIdNum = +gameIdFromQuery;
            const gameFromCollection = this.gamesSharedCollection.find(g => g.id === gameIdNum);

            if (gameFromCollection) {
              this.editForm.get('game')?.setValue(gameFromCollection);
              this.editForm.get('game')?.disable();
            } else {
              this.gameService.find(gameIdNum).subscribe(
                (res: HttpResponse<IGame>) => {
                  if (res.body) {
                    this.editForm.get('game')?.setValue(res.body);
                    this.editForm.get('game')?.disable();
                  }
                },
                error => console.error(`Erro ao buscar jogo com ID ${gameIdFromQuery}:`, error),
              );
            }
          } else {
            this.editForm.get('game')?.enable();
          }
        }),
      )
      .subscribe({
        error: err => console.error('Erro no pipeline RxJS:', err),
      });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const item = this.itemFormService.getItem(this.editForm);
    if (item.id !== null) {
      this.subscribeToSaveResponse(this.itemService.update(item));
    } else {
      this.subscribeToSaveResponse(this.itemService.create(item));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IItem>>): void {
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

  protected updateForm(item: IItem): void {
    this.item = item;
    this.itemFormService.resetForm(this.editForm, item);

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(
      this.usersSharedCollection,
      item.owner,
      item.lendedTo,
    );
    this.gamesSharedCollection = this.gameService.addGameToCollectionIfMissing<IGame>(this.gamesSharedCollection, item.game);
    this.editForm.get('game')?.enable();
  }

  protected loadRelationshipsOptionsObservables(initialItem: IItem | null, currentUserForOwner: any): Observable<[IUser[], IGame[]]> {
    const userQuery$ = this.userService.query().pipe(
      map((res: HttpResponse<IUser[]>) => res.body ?? []),
      map((users: IUser[]) =>
        this.userService.addUserToCollectionIfMissing<IUser>(
          users,
          initialItem?.owner,
          initialItem?.lendedTo,
          currentUserForOwner ? (currentUserForOwner as IUser) : undefined,
        ),
      ),
    );

    const gameQuery$ = this.gameService.query().pipe(
      map((res: HttpResponse<IGame[]>) => res.body ?? []),
      map((games: IGame[]) => this.gameService.addGameToCollectionIfMissing<IGame>(games, initialItem?.game)),
    );

    return forkJoin([userQuery$, gameQuery$]);
  }
}
