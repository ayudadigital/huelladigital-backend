import * as React from 'react';
import './styles.scss';
import { InputFieldFormProps } from './types';
import { ChangeEvent, useState } from 'react';

export const InputFieldForm: React.FC<InputFieldFormProps> = ({
  type,
  name,
  value,
  onChange,
}) => {
  const [check, setCheck] = useState('');

  const checkLength = (event: ChangeEvent<HTMLInputElement>) => {
    const minLenght: number = 6;
    let password = event.target.value;
    let type = event.target.type;
    if (type === 'password' && password.length >= minLenght) {
      setCheck('correct');
    } else {
      setCheck('incorrect');
    }
  };

  return (
    <input
      className={`InputFieldForm ${check}`}
      aria-label={'input-form'}
      type={type}
      name={name}
      value={value}
      onChange={(onChange) => {
        checkLength(onChange);
      }}
    />
  );
};

InputFieldForm.displayName = 'InputFieldForm';
