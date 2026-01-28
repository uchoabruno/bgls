import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { ItemComponent } from './list/item.component';
import { ItemDetailComponent } from './detail/item-detail.component';
import { ItemUpdateComponent } from './update/item-update.component';
import ItemResolve from './route/item-routing-resolve.service';

const itemRoute: Routes = [
  {
    path: '',
    component: ItemComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ItemDetailComponent,
    resolve: {
      item: ItemResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ItemUpdateComponent,
    resolve: {
      item: ItemResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ItemUpdateComponent,
    resolve: {
      item: ItemResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default itemRoute;
