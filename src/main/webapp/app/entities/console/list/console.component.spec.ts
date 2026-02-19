import { ComponentFixture, fakeAsync, inject, TestBed, tick } from '@angular/core/testing';
import { HttpHeaders, HttpResponse, provideHttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { sampleWithRequiredData } from '../console.test-samples';
import { ConsoleService } from '../service/console.service';

import { ConsoleComponent } from './console.component';
import { TranslateService } from '@ngx-translate/core';
import { StateStorageService } from '../../../core/auth/state-storage.service';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import SpyInstance = jest.SpyInstance;

describe('Console Management Component', () => {
  let comp: ConsoleComponent;
  let fixture: ComponentFixture<ConsoleComponent>;
  let service: ConsoleService;
  let routerNavigateSpy: SpyInstance<Promise<boolean>>;

  const mockTranslateService = {
    instant(key: string) {
      return key;
    },
    get(key: string) {
      return of(key);
    },
    onLangChange: of({}),
    use(lang: string) {
      return of(lang);
    },
    currentLang: 'en',
  };

  const mockStateStorageService = {
    getLocale() {
      return 'en';
    },
    setLocale(locale: string) {},
  };

  const mockApplicationConfigService = {
    getEndpointFor(api: string) {
      return `/api/${api}`;
    },
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ConsoleComponent],
      providers: [
        provideHttpClient(),
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({
              defaultSort: 'id,asc',
            }),
            queryParamMap: of(
              jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
              }),
            ),
            snapshot: {
              queryParams: {},
              queryParamMap: jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
              }),
            },
          },
        },
        // --- ADIÇÕES DE PROVIDERS START ---
        { provide: TranslateService, useValue: mockTranslateService },
        { provide: StateStorageService, useValue: mockStateStorageService },
        { provide: ApplicationConfigService, useValue: mockApplicationConfigService },
        // --- ADIÇÕES DE PROVIDERS END ---
      ],
    })
      .overrideTemplate(ConsoleComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ConsoleComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(ConsoleService);
    routerNavigateSpy = jest.spyOn(comp.router, 'navigate');

    jest
      .spyOn(service, 'query')
      .mockReturnValueOnce(
        of(
          new HttpResponse({
            body: [{ id: 123 }],
            headers: new HttpHeaders({
              link: '<http://localhost/api/foo?page=1&size=20>; rel="next"',
            }),
          }),
        ),
      )
      .mockReturnValueOnce(
        of(
          new HttpResponse({
            body: [{ id: 456 }],
            headers: new HttpHeaders({
              link: '<http://localhost/api/foo?page=0&size=20>; rel="prev",<http://localhost/api/foo?page=2&size=20>; rel="next"',
            }),
          }),
        ),
      );
  });

  it('Should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.consoles?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to consoleService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getConsoleIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getConsoleIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });

  it('should calculate the sort attribute for a non-id attribute', () => {
    // WHEN
    comp.navigateToWithComponentValues({ predicate: 'non-existing-column', order: 'asc' });

    // THEN
    expect(routerNavigateSpy).toHaveBeenLastCalledWith(
      expect.anything(),
      expect.objectContaining({
        queryParams: expect.objectContaining({
          sort: ['non-existing-column,asc'],
        }),
      }),
    );
  });

  it('should calculate the sort attribute for an id', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenLastCalledWith(expect.objectContaining({ sort: ['id,desc'] }));
  });

  describe('delete', () => {
    let ngbModal: NgbModal;
    let deleteModalMock: any;

    beforeEach(() => {
      deleteModalMock = { componentInstance: {}, closed: new Subject() };
      // NgbModal is not a singleton using TestBed.inject.
      // ngbModal = TestBed.inject(NgbModal);
      ngbModal = (comp as any).modalService;
      jest.spyOn(ngbModal, 'open').mockReturnValue(deleteModalMock);
    });

    it('on confirm should call load', inject(
      [],
      fakeAsync(() => {
        // GIVEN
        jest.spyOn(comp, 'load');

        // WHEN
        comp.delete(sampleWithRequiredData);
        deleteModalMock.closed.next('deleted');
        tick();

        // THEN
        expect(ngbModal.open).toHaveBeenCalled();
        expect(comp.load).toHaveBeenCalled();
      }),
    ));

    it('on dismiss should call load', inject(
      [],
      fakeAsync(() => {
        // GIVEN
        jest.spyOn(comp, 'load');

        // WHEN
        comp.delete(sampleWithRequiredData);
        deleteModalMock.closed.next();
        tick();

        // THEN
        expect(ngbModal.open).toHaveBeenCalled();
        expect(comp.load).not.toHaveBeenCalled();
      }),
    ));
  });
});
