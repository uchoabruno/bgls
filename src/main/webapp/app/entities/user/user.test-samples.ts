import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 19374,
  login: '1VKfk',
};

export const sampleWithPartialData: IUser = {
  id: 31660,
  login: 'F6zNUd',
};

export const sampleWithFullData: IUser = {
  id: 20689,
  login: '8ialU4',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
