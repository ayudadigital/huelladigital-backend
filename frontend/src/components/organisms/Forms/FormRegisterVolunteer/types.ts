import React from 'react';
import { stateValidateTypes } from '../../../atoms/InputFieldForm/types';

export interface DataInterface {
  email: string | React.ChangeEvent<HTMLInputElement>;
  password: string | React.ChangeEvent<HTMLInputElement>;
  passwordRepeated: string | React.ChangeEvent<HTMLInputElement>;
}

export interface CheckInterface {
  email: stateValidateTypes;
  password: stateValidateTypes;
  passwordRepeated: stateValidateTypes;
}
