import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { GameComponent } from './list/game.component';
import { GameDetailComponent } from './detail/game-detail.component';
import { GameUpdateComponent } from './update/game-update.component';
import GameResolve from './route/game-routing-resolve.service';

const gameRoute: Routes = [
  {
    path: '',
    component: GameComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: GameDetailComponent,
    resolve: {
      game: GameResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: GameUpdateComponent,
    resolve: {
      game: GameResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: GameUpdateComponent,
    resolve: {
      game: GameResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default gameRoute;
