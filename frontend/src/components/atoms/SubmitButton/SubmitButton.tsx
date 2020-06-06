import * as React from 'react';
import './styles.scss';
import { SubmitButtonProps } from './types';
import { Link } from 'react-router-dom';

export const SubmitButton: React.FC<SubmitButtonProps> = ({text='', disabled = false, to = '/#'}) => (
  <Link to={to}>
  <button className="SubmitButton"
          aria-label={'submit-button'}
          type={'submit'}
          disabled={disabled}>
    {text}
  </button>
  </Link>
);

SubmitButton.displayName = 'SubmitButton';
