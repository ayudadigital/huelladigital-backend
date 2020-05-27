import * as React from 'react';
import './styles.scss';
import { InputFieldFormProps } from './types';

export const InputFieldForm: React.FC<InputFieldFormProps> = ({
  type,
  name,
  value,
  onChange,
}) => {
  const checkPassword = (pass: String, passrepeated: String) => {
    if (checkLength(pass)) {
      console.log('añadir color verde');
    } else {
      console.log('añadir color rojo');
    }
  };

  const checkLength = (password: String) => {
    const minLenght: number = 5;
    let comprobar: boolean = false;
    if (password.length >= minLenght) comprobar = true;
    return comprobar;
  };

  return (
    <input
      className="InputFieldForm"
      aria-label={'input-form'}
      type={type}
      name={name}
      value={value}
      onChange={onChange}
    />
  );
};

InputFieldForm.displayName = 'InputFieldForm';
