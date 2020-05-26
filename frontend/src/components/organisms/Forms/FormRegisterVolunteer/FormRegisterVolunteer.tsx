import * as React from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';

export const FormRegisterVolunteer: React.FC<{}> = () => (
  <form className="ContainerForm">
    <h1>Registro de voluntario</h1>
    <FieldForm title={'Email'} type={'email'} name={'email'} />
    <FieldForm title={'Contraseña'} type={'password'} name={'password'} />
    <FieldForm title={'Repetir contraseña'} type={'password'} name={'repeatPassword'} />
    <label htmlFor="privacity">
      <input type="checkbox" name="privacity" />
      Acepto la política de privacidad del sitio.
    </label>
    <SubmitButton text={'Acceder'} />
    <p>¿Ya tiene cuenta? Iniciar sesión</p>
  </form>
);

FormRegisterVolunteer.displayName = 'FormRegisterVolunteer';
