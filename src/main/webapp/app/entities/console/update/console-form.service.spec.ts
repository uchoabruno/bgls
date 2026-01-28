import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../console.test-samples';

import { ConsoleFormService } from './console-form.service';

describe('Console Form Service', () => {
  let service: ConsoleFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConsoleFormService);
  });

  describe('Service methods', () => {
    describe('createConsoleFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createConsoleFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            image: expect.any(Object),
          }),
        );
      });

      it('passing IConsole should create a new form with FormGroup', () => {
        const formGroup = service.createConsoleFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            image: expect.any(Object),
          }),
        );
      });
    });

    describe('getConsole', () => {
      it('should return NewConsole for default Console initial value', () => {
        const formGroup = service.createConsoleFormGroup(sampleWithNewData);

        const console = service.getConsole(formGroup) as any;

        expect(console).toMatchObject(sampleWithNewData);
      });

      it('should return NewConsole for empty Console initial value', () => {
        const formGroup = service.createConsoleFormGroup();

        const console = service.getConsole(formGroup) as any;

        expect(console).toMatchObject({});
      });

      it('should return IConsole', () => {
        const formGroup = service.createConsoleFormGroup(sampleWithRequiredData);

        const console = service.getConsole(formGroup) as any;

        expect(console).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IConsole should not enable id FormControl', () => {
        const formGroup = service.createConsoleFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewConsole should disable id FormControl', () => {
        const formGroup = service.createConsoleFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
