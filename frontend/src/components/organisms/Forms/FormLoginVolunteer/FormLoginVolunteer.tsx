import React from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';
import { LinkText } from '../../../atoms/LinkText';
import { ROUTE } from '../../../../utils/routes';

export const FormLoginVolunteer: React.FC<{}> = () => {
  return (
    <form className="ContainerForm" method="POST" id="form">
      <h1>Acceso de voluntario</h1>
      <FieldForm title={'Email'} type={'email'} name={'email'} />
      <FieldForm title={'Contraseña'} type={'password'} name={'password'} />
      <SubmitButton text={'Acceder'} />
      <p>
        ¿No tiene una cuenta?
        <LinkText to={ROUTE.volunteer.register} text={'Registrate'} />
      </p>
    </form>
  );
};

FormLoginVolunteer.displayName = 'FormLoginVolunteer';
