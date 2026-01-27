import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: '00dad422-9dc7-47aa-bc14-6d72a9a88cb7',
};

export const sampleWithPartialData: IAuthority = {
  name: 'f7d902d3-73d4-46ed-9504-40d8969de5a9',
};

export const sampleWithFullData: IAuthority = {
  name: '2c9f2500-8369-4c94-aa2f-0d2254f663b7',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
