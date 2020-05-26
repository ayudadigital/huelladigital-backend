import React from 'react';
import './styles.scss';
import { FieldFormProps } from './types';
import { Label } from '../../atoms/Label';
import { InputFieldForm } from '../../atoms/InputFieldForm';

export const FieldForm: React.FC<FieldFormProps> = ({
  title = '',
  type = 'text',
  name = '',
  value,
  getData,
}) => {
  const handleInput = (event: any) => {
    event.preventDefault();
    const inputName = event.target.name;
    const inputValue = event.target.value;
    getData([inputName, inputValue]);
  };

  return (
    <div className="FieldForm">
      <Label text={title} />
      <InputFieldForm type={type} name={name} value={value} onChangeValue={handleInput} />
    </div>
  );
};

FieldForm.displayName = 'FieldForm';
