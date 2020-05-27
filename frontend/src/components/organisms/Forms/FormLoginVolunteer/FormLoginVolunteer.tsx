import React from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';

export const FormLoginVolunteer: React.FC<{}> = () => {
  return (
    <form className="ContainerForm" method="POST" id="form">
      <h1>Acceso de voluntario</h1>
      <FieldForm title={'Email'} type={'email'} name={'email'} />
      <FieldForm title={'Contraseña'} type={'password'} name={'password'} />
      <SubmitButton text={'Acceder'} />
      <p>¿Ya tiene cuenta? Iniciar sesión</p>
    </form>
  );
};

FormLoginVolunteer.displayName = 'FormLoginVolunteer';
