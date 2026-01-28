import { IGame, NewGame } from './game.model';

export const sampleWithRequiredData: IGame = {
  id: 15843,
  name: 'duh',
};

export const sampleWithPartialData: IGame = {
  id: 26482,
  name: 'boo',
};

export const sampleWithFullData: IGame = {
  id: 3371,
  name: 'athwart whenever nutritious',
  cover: '../fake-data/blob/hipster.png',
  coverContentType: 'unknown',
};

export const sampleWithNewData: NewGame = {
  name: 'certainly whenever',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
