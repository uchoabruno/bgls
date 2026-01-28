import { IConsole } from 'app/entities/console/console.model';

export interface IGame {
  id: number;
  name?: string | null;
  cover?: string | null;
  coverContentType?: string | null;
  console?: Pick<IConsole, 'id' | 'name'> | null;
}

export type NewGame = Omit<IGame, 'id'> & { id: null };
