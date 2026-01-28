import { IConsole, NewConsole } from './console.model';

export const sampleWithRequiredData: IConsole = {
  id: 1157,
  name: 'unnaturally meanwhile',
};

export const sampleWithPartialData: IConsole = {
  id: 9712,
  name: 'lid',
  image: '../fake-data/blob/hipster.png',
  imageContentType: 'unknown',
};

export const sampleWithFullData: IConsole = {
  id: 32227,
  name: 'voting ferry',
  image: '../fake-data/blob/hipster.png',
  imageContentType: 'unknown',
};

export const sampleWithNewData: NewConsole = {
  name: 'hence wholly hoax',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
