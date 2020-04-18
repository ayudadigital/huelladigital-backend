import * as React from 'react';
import './FieldForm.scss';
import { FieldFormProps } from './types';

export const FieldForm: React.FC<FieldFormProps> = ({title = '', type= 'text', name= ''}) => (
  <div className="FieldForm">
    <label>{title}:</label>
    <input type={type} name={name}/>
  </div>
);

FieldForm.displayName = 'FieldForm';
