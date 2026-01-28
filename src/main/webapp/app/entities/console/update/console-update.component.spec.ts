import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { ConsoleService } from '../service/console.service';
import { IConsole } from '../console.model';
import { ConsoleFormService } from './console-form.service';

import { ConsoleUpdateComponent } from './console-update.component';

describe('Console Management Update Component', () => {
  let comp: ConsoleUpdateComponent;
  let fixture: ComponentFixture<ConsoleUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let consoleFormService: ConsoleFormService;
  let consoleService: ConsoleService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ConsoleUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(ConsoleUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ConsoleUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    consoleFormService = TestBed.inject(ConsoleFormService);
    consoleService = TestBed.inject(ConsoleService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const console: IConsole = { id: 456 };

      activatedRoute.data = of({ console });
      comp.ngOnInit();

      expect(comp.console).toEqual(console);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IConsole>>();
      const console = { id: 123 };
      jest.spyOn(consoleFormService, 'getConsole').mockReturnValue(console);
      jest.spyOn(consoleService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ console });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: console }));
      saveSubject.complete();

      // THEN
      expect(consoleFormService.getConsole).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(consoleService.update).toHaveBeenCalledWith(expect.objectContaining(console));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IConsole>>();
      const console = { id: 123 };
      jest.spyOn(consoleFormService, 'getConsole').mockReturnValue({ id: null });
      jest.spyOn(consoleService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ console: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: console }));
      saveSubject.complete();

      // THEN
      expect(consoleFormService.getConsole).toHaveBeenCalled();
      expect(consoleService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IConsole>>();
      const console = { id: 123 };
      jest.spyOn(consoleService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ console });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(consoleService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
