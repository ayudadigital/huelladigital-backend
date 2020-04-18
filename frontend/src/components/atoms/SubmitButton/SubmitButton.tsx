import * as React from 'react';
import './SubmitButton.scss';
import { SubmitButtonProps } from './types';

export const SubmitButton: React.FC<SubmitButtonProps> = ({text='',}) => (
  <button className="SubmitButton" type={'submit'}>
    {text}
  </button>
);

SubmitButton.displayName = 'SubmitButton';
