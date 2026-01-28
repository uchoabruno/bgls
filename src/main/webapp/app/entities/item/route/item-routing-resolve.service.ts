import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IItem } from '../item.model';
import { ItemService } from '../service/item.service';

const itemResolve = (route: ActivatedRouteSnapshot): Observable<null | IItem> => {
  const id = route.params['id'];
  if (id) {
    return inject(ItemService)
      .find(id)
      .pipe(
        mergeMap((item: HttpResponse<IItem>) => {
          if (item.body) {
            return of(item.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default itemResolve;
