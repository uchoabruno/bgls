import { IGame, NewGame } from './game.model';

export const sampleWithRequiredData: IGame = {
  id: 1944,
  name: 'overindulge',
};

export const sampleWithPartialData: IGame = {
  id: 18143,
  name: 'near annually remorseful',
  cover: '../fake-data/blob/hipster.png',
  coverContentType: 'unknown',
};

export const sampleWithFullData: IGame = {
  id: 13778,
  name: 'than gee drat',
  cover: '../fake-data/blob/hipster.png',
  coverContentType: 'unknown',
};

export const sampleWithNewData: NewGame = {
  name: 'though',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
