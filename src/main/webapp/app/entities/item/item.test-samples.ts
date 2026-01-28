import { IItem, NewItem } from './item.model';

export const sampleWithRequiredData: IItem = {
  id: 17469,
};

export const sampleWithPartialData: IItem = {
  id: 10009,
};

export const sampleWithFullData: IItem = {
  id: 22718,
};

export const sampleWithNewData: NewItem = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
