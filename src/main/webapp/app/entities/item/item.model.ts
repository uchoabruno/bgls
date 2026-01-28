import { IUser } from 'app/entities/user/user.model';
import { IGame } from 'app/entities/game/game.model';

export interface IItem {
  id: number;
  owner?: Pick<IUser, 'id' | 'login'> | null;
  lendedTo?: Pick<IUser, 'id' | 'login'> | null;
  game?: IGame | null;
}

export type NewItem = Omit<IItem, 'id'> & { id: null };
