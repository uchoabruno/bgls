import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../game.test-samples';

import { GameFormService } from './game-form.service';

describe('Game Form Service', () => {
  let service: GameFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GameFormService);
  });

  describe('Service methods', () => {
    describe('createGameFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createGameFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            cover: expect.any(Object),
            console: expect.any(Object),
          }),
        );
      });

      it('passing IGame should create a new form with FormGroup', () => {
        const formGroup = service.createGameFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            cover: expect.any(Object),
            console: expect.any(Object),
          }),
        );
      });
    });

    describe('getGame', () => {
      it('should return NewGame for default Game initial value', () => {
        const formGroup = service.createGameFormGroup(sampleWithNewData);

        const game = service.getGame(formGroup) as any;

        expect(game).toMatchObject(sampleWithNewData);
      });

      it('should return NewGame for empty Game initial value', () => {
        const formGroup = service.createGameFormGroup();

        const game = service.getGame(formGroup) as any;

        expect(game).toMatchObject({});
      });

      it('should return IGame', () => {
        const formGroup = service.createGameFormGroup(sampleWithRequiredData);

        const game = service.getGame(formGroup) as any;

        expect(game).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IGame should not enable id FormControl', () => {
        const formGroup = service.createGameFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewGame should disable id FormControl', () => {
        const formGroup = service.createGameFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
