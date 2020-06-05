import * as React from 'react';
import './styles.scss';
import { SubmitButtonProps } from './types';

export const SubmitButton: React.FC<SubmitButtonProps> = ({text='', disabled = false}) => (
  <button className="SubmitButton" type={'submit'} disabled={disabled}>
    {text}
  </button>
);

SubmitButton.displayName = 'SubmitButton';
