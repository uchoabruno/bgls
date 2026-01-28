import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IConsole, NewConsole } from '../console.model';

export type PartialUpdateConsole = Partial<IConsole> & Pick<IConsole, 'id'>;

export type EntityResponseType = HttpResponse<IConsole>;
export type EntityArrayResponseType = HttpResponse<IConsole[]>;

@Injectable({ providedIn: 'root' })
export class ConsoleService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/consoles');

  create(console: NewConsole): Observable<EntityResponseType> {
    return this.http.post<IConsole>(this.resourceUrl, console, { observe: 'response' });
  }

  update(console: IConsole): Observable<EntityResponseType> {
    return this.http.put<IConsole>(`${this.resourceUrl}/${this.getConsoleIdentifier(console)}`, console, { observe: 'response' });
  }

  partialUpdate(console: PartialUpdateConsole): Observable<EntityResponseType> {
    return this.http.patch<IConsole>(`${this.resourceUrl}/${this.getConsoleIdentifier(console)}`, console, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IConsole>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IConsole[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getConsoleIdentifier(console: Pick<IConsole, 'id'>): number {
    return console.id;
  }

  compareConsole(o1: Pick<IConsole, 'id'> | null, o2: Pick<IConsole, 'id'> | null): boolean {
    return o1 && o2 ? this.getConsoleIdentifier(o1) === this.getConsoleIdentifier(o2) : o1 === o2;
  }

  addConsoleToCollectionIfMissing<Type extends Pick<IConsole, 'id'>>(
    consoleCollection: Type[],
    ...consolesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const consoles: Type[] = consolesToCheck.filter(isPresent);
    if (consoles.length > 0) {
      const consoleCollectionIdentifiers = consoleCollection.map(consoleItem => this.getConsoleIdentifier(consoleItem));
      const consolesToAdd = consoles.filter(consoleItem => {
        const consoleIdentifier = this.getConsoleIdentifier(consoleItem);
        if (consoleCollectionIdentifiers.includes(consoleIdentifier)) {
          return false;
        }
        consoleCollectionIdentifiers.push(consoleIdentifier);
        return true;
      });
      return [...consolesToAdd, ...consoleCollection];
    }
    return consoleCollection;
  }
}
