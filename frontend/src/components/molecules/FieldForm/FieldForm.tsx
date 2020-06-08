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
                                                      onChange,
                                                      onBlur,
                                                      stateValidate,
                                                      messageInfoUser
                                                    }) => {
  return (
    <div className="FieldForm">
      <Label text={title}/>
      <InputFieldForm
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        stateValidate={stateValidate}
      />
      {stateValidate === 'incorrect' && <p aria-label={'message-error'}>{messageInfoUser}</p>}
    </div>
  );
};

FieldForm.displayName = 'FieldForm';
