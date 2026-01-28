import { IConsole, NewConsole } from './console.model';

export const sampleWithRequiredData: IConsole = {
  id: 23996,
  name: 'helplessly reorganization',
};

export const sampleWithPartialData: IConsole = {
  id: 29390,
  name: 'wisely',
  image: '../fake-data/blob/hipster.png',
  imageContentType: 'unknown',
};

export const sampleWithFullData: IConsole = {
  id: 14259,
  name: 'calmly equal',
  image: '../fake-data/blob/hipster.png',
  imageContentType: 'unknown',
};

export const sampleWithNewData: NewConsole = {
  name: 'outclass motion',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
