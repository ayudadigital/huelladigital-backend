import React from 'react';

export interface InputFieldFormProps {
  type: inputTypes | string;
  name: string;
  value?: string;
  onChange?: (event: React.ChangeEvent<HTMLInputElement>) => string | void;
  onBlur?: any;
  stateValidate?: stateValidateTypes;
}

export type inputTypes = 'email' | 'password' | 'text';
export type stateValidateTypes = '' | 'correct' | 'incorrect';
