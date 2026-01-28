import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { ConsoleComponent } from './list/console.component';
import { ConsoleDetailComponent } from './detail/console-detail.component';
import { ConsoleUpdateComponent } from './update/console-update.component';
import ConsoleResolve from './route/console-routing-resolve.service';

const consoleRoute: Routes = [
  {
    path: '',
    component: ConsoleComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ConsoleDetailComponent,
    resolve: {
      console: ConsoleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ConsoleUpdateComponent,
    resolve: {
      console: ConsoleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ConsoleUpdateComponent,
    resolve: {
      console: ConsoleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default consoleRoute;
