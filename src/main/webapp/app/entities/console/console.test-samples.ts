import { IConsole, NewConsole } from './console.model';

export const sampleWithRequiredData: IConsole = {
  id: 22022,
  name: 'after stretcher',
};

export const sampleWithPartialData: IConsole = {
  id: 31971,
  name: 'resident only once',
};

export const sampleWithFullData: IConsole = {
  id: 8565,
  name: 'furthermore provided',
  image: '../fake-data/blob/hipster.png',
  imageContentType: 'unknown',
};

export const sampleWithNewData: NewConsole = {
  name: 'reverberate',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
