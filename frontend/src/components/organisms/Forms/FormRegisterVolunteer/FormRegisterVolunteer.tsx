import * as React from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';

export const FormRegisterVolunteer: React.FC<{}> = () => (
  <form className="ContainerForm">
    <h1>Registro de voluntario</h1>
    <FieldForm title={'Email'} type={'email'} name={'email'}/>
    <FieldForm title={'Contraseña'} type={'password'} name={'password'}/>
    <FieldForm title={'Vuelva a poner la contraseña'} type={'password'} name={'repeatPassword'}/>
    <SubmitButton text={'Acceder'}/>
  </form>
);

FormRegisterVolunteer.displayName = 'FormRegisterVolunteer';
