import * as React from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';

export const FormRegisterOrganization: React.FC<{}> = () => (
  <form className="ContainerForm">
    <h1>Registro de organización</h1>
    ¿Perteneces a una entidad o eres una persona con una iniciativa particular?
    <label htmlFor="person">
      <input type="radio" id="person" name="helpType" value="person" />
      Persona física
    </label>
    <label htmlFor="entity">
      <input type="radio" id="entity" name="helpType" value="entity" />
      Entidad
    </label>
    <FieldForm title={'Email'} type={'email'} name={'email'} />
    <FieldForm title={'Contraseña'} type={'password'} name={'password'} />
    <FieldForm
      title={'Vuelva a poner la contraseña'}
      type={'password'}
      name={'repeatPassword'}
    />
    <label htmlFor="privacity">
      <input type="checkbox" name="privacity" />
      Acepto la política de privacidad del sitio.
    </label>
    <SubmitButton text={'Acceder'} />
    <p>¿Ya tiene cuenta como organización? Iniciar sesión</p>
  </form>
);
