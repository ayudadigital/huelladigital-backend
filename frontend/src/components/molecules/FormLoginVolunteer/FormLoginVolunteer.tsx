import * as React from 'react';
import './FormLoginVolunteer.scss';
import { FieldForm } from '../../atoms/FieldForm';
import { SubmitButton } from '../../atoms/SubmitButton';

export const FormLoginVolunteer: React.FC<{}> = () => (
  <form className="FormLoginVolunteer">
    <h1>Acceso de voluntario</h1>
    <FieldForm title={'Email'} type={'email'} name={'email'}/>
    <FieldForm title={'Contraseña'} type={'password'} name={'password'}/>
    <SubmitButton text={'Acceder'}/>
  </form>
);

FormLoginVolunteer.displayName = 'FormLoginVolunteer';
