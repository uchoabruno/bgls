import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, convertToParamMap } from '@angular/router'; // <-- Adicione convertToParamMap aqui
import { from, of, Subject } from 'rxjs';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IGame } from 'app/entities/game/game.model';
import { GameService } from 'app/entities/game/service/game.service';
import { IItem } from '../item.model';
import { ItemService } from '../service/item.service';
import { ItemFormService } from './item-form.service';

import { ItemUpdateComponent } from './item-update.component';
import { TranslateModule } from '@ngx-translate/core';
import { AccountService } from '../../../core/auth/account.service';
import { Account } from '../../../core/auth/account.model';

describe('Item Management Update Component', () => {
  let comp: ItemUpdateComponent;
  let fixture: ComponentFixture<ItemUpdateComponent>;
  let accountService: AccountService;
  let activatedRoute: ActivatedRoute;
  let itemFormService: ItemFormService;
  let itemService: ItemService;
  let userService: UserService;
  let gameService: GameService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ItemUpdateComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
            data: of({}),
            queryParamMap: of(convertToParamMap({})),
          },
        },
      ],
    })
      .overrideTemplate(ItemUpdateComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(ItemUpdateComponent);
    accountService = TestBed.inject(AccountService);
    activatedRoute = TestBed.inject(ActivatedRoute);
    itemFormService = TestBed.inject(ItemFormService);
    itemService = TestBed.inject(ItemService);
    userService = TestBed.inject(UserService);
    gameService = TestBed.inject(GameService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call User query and add missing value (for new item)', async () => {
      const mockCurrentUser: Account = {
        login: 'testuser',
        activated: true,
        authorities: ['ROLE_USER'],
        email: 'test@example.com',
        firstName: 'Test',
        langKey: 'en',
        lastName: 'User',
        imageUrl: null,
      };
      const mockUserFromQuery: IUser = { id: 2, login: 'queriedUser' };
      const mockGame: IGame = { id: 10, name: 'Test Game' };

      jest.spyOn(accountService, 'identity').mockReturnValue(of(mockCurrentUser));
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: [mockUserFromQuery] })));
      jest.spyOn(gameService, 'query').mockReturnValue(of(new HttpResponse({ body: [mockGame] })));
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockImplementation((users, owner, lendedTo, currentUserOwner) => {
        const collection = [...users];
        if (currentUserOwner && !collection.some(u => u.id === currentUserOwner.id)) {
          collection.push(currentUserOwner);
        }
        return collection;
      });

      jest.spyOn(gameService, 'addGameToCollectionIfMissing').mockImplementation((games, gameToAdd) => {
        const collection = [...games];
        if (gameToAdd && !collection.some(g => g.id === gameToAdd.id)) {
          collection.push(gameToAdd);
        }
        return collection;
      });

      const expectedCollectionAfterAdd = [mockUserFromQuery];

      activatedRoute.data = of({ item: null });

      comp.ngOnInit();
      fixture.detectChanges();
      await fixture.whenStable();

      expect(accountService.identity).toHaveBeenCalled();
      expect(userService.query).toHaveBeenCalledWith({ 'login.equals': mockCurrentUser.login });
      expect(gameService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith([mockUserFromQuery], undefined, undefined, mockUserFromQuery);
      expect(comp.usersSharedCollection).toEqual(expectedCollectionAfterAdd);
      expect(comp.gamesSharedCollection).toEqual([mockGame]);
    });

    it('Should call Game query and add missing value', () => {
      const item: IItem = { id: 456 };
      const game: IGame = { id: 24051 };
      item.game = game;

      const gameCollection: IGame[] = [{ id: 4512 }];
      jest.spyOn(gameService, 'query').mockReturnValue(of(new HttpResponse({ body: gameCollection })));
      const additionalGames = [game];
      const expectedCollection: IGame[] = [...additionalGames, ...gameCollection];
      jest.spyOn(gameService, 'addGameToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ item });
      comp.ngOnInit();

      expect(gameService.query).toHaveBeenCalled();
      expect(gameService.addGameToCollectionIfMissing).toHaveBeenCalledWith(
        gameCollection,
        ...additionalGames.map(expect.objectContaining),
      );
      expect(comp.gamesSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const item: IItem = { id: 456 };
      const owner: IUser = { id: 4638 };
      item.owner = owner;
      const lendedTo: IUser = { id: 26265 };
      item.lendedTo = lendedTo;
      const game: IGame = { id: 3657 };
      item.game = game;

      activatedRoute.data = of({ item });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContain(owner);
      expect(comp.usersSharedCollection).toContain(lendedTo);
      expect(comp.gamesSharedCollection).toContain(game);
      expect(comp.item).toEqual(item);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IItem>>();
      const item = { id: 123 };
      jest.spyOn(itemFormService, 'getItem').mockReturnValue(item);
      jest.spyOn(itemService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ item });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: item }));
      saveSubject.complete();

      // THEN
      expect(itemFormService.getItem).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(itemService.update).toHaveBeenCalledWith(expect.objectContaining(item));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IItem>>();
      const item = { id: 123 };
      jest.spyOn(itemFormService, 'getItem').mockReturnValue({ id: null });
      jest.spyOn(itemService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ item: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: item }));
      saveSubject.complete();

      // THEN
      expect(itemFormService.getItem).toHaveBeenCalled();
      expect(itemService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IItem>>();
      const item = { id: 123 };
      jest.spyOn(itemService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ item });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(itemService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('Should forward to userService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareGame', () => {
      it('Should forward to gameService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(gameService, 'compareGame');
        comp.compareGame(entity, entity2);
        expect(gameService.compareGame).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
