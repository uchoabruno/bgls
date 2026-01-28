import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IConsole } from '../console.model';
import { ConsoleService } from '../service/console.service';

const consoleResolve = (route: ActivatedRouteSnapshot): Observable<null | IConsole> => {
  const id = route.params['id'];
  if (id) {
    return inject(ConsoleService)
      .find(id)
      .pipe(
        mergeMap((console: HttpResponse<IConsole>) => {
          if (console.body) {
            return of(console.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default consoleResolve;
