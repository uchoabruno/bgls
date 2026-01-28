export interface IConsole {
  id: number;
  name?: string | null;
  image?: string | null;
  imageContentType?: string | null;
}

export type NewConsole = Omit<IConsole, 'id'> & { id: null };
