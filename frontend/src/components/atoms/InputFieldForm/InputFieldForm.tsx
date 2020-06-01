import * as React from 'react';
import './styles.scss';
import { InputFieldFormProps } from './types';
import { ChangeEvent, useState } from 'react';

export const InputFieldForm: React.FC<InputFieldFormProps> = ({
  type,
  name,
  value,
  onChange,
  onBlur,
}) => {
  const [check, setCheck] = useState('');

  const checkLength = (event: ChangeEvent<HTMLInputElement>) => {
    const minLenght: number = 6;
    const regexEmail = new RegExp(
      /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/,
    );
    let inputValue = event.target.value;
    let type = event.target.type;
    if (type !== 'email') {
    }

    switch (type) {
      case 'email':
        if (regexEmail.test(inputValue)) {
          setCheck('correct');
        } else {
          setCheck('incorrect');
        }
        break;
      case 'password':
        if (inputValue.length >= minLenght) {
          setCheck('correct');
        } else {
          setCheck('incorrect');
        }
        break;
      default:
        break;
    }
    return inputValue;
  };

  return (
    <input
      className={`InputFieldForm ${check}`}
      aria-label={'input-form'}
      type={type}
      name={name}
      value={value}
      //TODO: we need to change the value of checkLength
      onChange={(onChange) => {
        checkLength(onChange);
      }}
      onBlur={onBlur}
    />
  );
};

InputFieldForm.displayName = 'InputFieldForm';
