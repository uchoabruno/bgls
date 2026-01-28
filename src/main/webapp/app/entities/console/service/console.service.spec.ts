import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IConsole } from '../console.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../console.test-samples';

import { ConsoleService } from './console.service';

const requireRestSample: IConsole = {
  ...sampleWithRequiredData,
};

describe('Console Service', () => {
  let service: ConsoleService;
  let httpMock: HttpTestingController;
  let expectedResult: IConsole | IConsole[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ConsoleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Console', () => {
      const console = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(console).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Console', () => {
      const console = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(console).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Console', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Console', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Console', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addConsoleToCollectionIfMissing', () => {
      it('should add a Console to an empty array', () => {
        const console: IConsole = sampleWithRequiredData;
        expectedResult = service.addConsoleToCollectionIfMissing([], console);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(console);
      });

      it('should not add a Console to an array that contains it', () => {
        const console: IConsole = sampleWithRequiredData;
        const consoleCollection: IConsole[] = [
          {
            ...console,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addConsoleToCollectionIfMissing(consoleCollection, console);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Console to an array that doesn't contain it", () => {
        const console: IConsole = sampleWithRequiredData;
        const consoleCollection: IConsole[] = [sampleWithPartialData];
        expectedResult = service.addConsoleToCollectionIfMissing(consoleCollection, console);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(console);
      });

      it('should add only unique Console to an array', () => {
        const consoleArray: IConsole[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const consoleCollection: IConsole[] = [sampleWithRequiredData];
        expectedResult = service.addConsoleToCollectionIfMissing(consoleCollection, ...consoleArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const console: IConsole = sampleWithRequiredData;
        const console2: IConsole = sampleWithPartialData;
        expectedResult = service.addConsoleToCollectionIfMissing([], console, console2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(console);
        expect(expectedResult).toContain(console2);
      });

      it('should accept null and undefined values', () => {
        const console: IConsole = sampleWithRequiredData;
        expectedResult = service.addConsoleToCollectionIfMissing([], null, console, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(console);
      });

      it('should return initial array if no Console is added', () => {
        const consoleCollection: IConsole[] = [sampleWithRequiredData];
        expectedResult = service.addConsoleToCollectionIfMissing(consoleCollection, undefined, null);
        expect(expectedResult).toEqual(consoleCollection);
      });
    });

    describe('compareConsole', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareConsole(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareConsole(entity1, entity2);
        const compareResult2 = service.compareConsole(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareConsole(entity1, entity2);
        const compareResult2 = service.compareConsole(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareConsole(entity1, entity2);
        const compareResult2 = service.compareConsole(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
