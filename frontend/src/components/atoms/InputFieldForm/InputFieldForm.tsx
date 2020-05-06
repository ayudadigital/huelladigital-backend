import * as React from 'react';
import './styles.scss';
import { InputFieldFormProps } from './types';

export const InputFieldForm: React.FC<InputFieldFormProps> = ({type, name, value}) => (
    <input className="InputFieldForm"
           aria-label={'input-form'}
           type={type}
           name={name}
           value={value}
    />
);

InputFieldForm.displayName = 'InputFieldForm';
