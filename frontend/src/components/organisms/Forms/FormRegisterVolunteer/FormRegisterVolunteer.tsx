import React, { ChangeEvent, useState } from 'react';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';
import { ROUTE } from '../../../../utils/routes';
import { LinkText } from '../../../atoms/LinkText';
import Client from '../FormUtils/client';
import '../styles.scss';
import { stateValidateTypes } from '../../../atoms/InputFieldForm/types';

interface dataInterface {
  email: string | React.ChangeEvent<HTMLInputElement>;
  password: string | React.ChangeEvent<HTMLInputElement>;
  passwordRepeated: string | React.ChangeEvent<HTMLInputElement>;
}

export const FormRegisterVolunteer: React.FC<{}> = () => {
  const [data, setData] = useState<dataInterface>({
    email: '',
    password: '',
    passwordRepeated: '',
  });

  const handleSubmit = (event: any) => {
    event.preventDefault();
    const exampleJsonToApi = {
      email: data.email,
      password: data.password,
    };
    const client = new Client();
    client.registerVolunteer(exampleJsonToApi);
  };


  const [check, setCheck] = useState<{ email: stateValidateTypes, password: stateValidateTypes, passwordRepeated: stateValidateTypes }>({
    email: '',
    password: '',
    passwordRepeated: '',
  });

  const checkPassword = () => {
    if (data.password === data.passwordRepeated) {
      setCheck({ ...check, passwordRepeated: 'correct' });
    } else {
      setCheck({ ...check, passwordRepeated: 'incorrect' });
    }
  };

  const checkLength: (event: ChangeEvent<HTMLInputElement>) => void = (event: ChangeEvent<HTMLInputElement>) => {
    const minLength: number = 6;
    const regexEmail = new RegExp(
      /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/,
    );
    const inputValue = event.target.value;
    const nameEvent = event.target.name;

    switch (nameEvent) {
      case 'email':
        if (regexEmail.test(inputValue)) {
          setCheck({ ...check, email: 'correct' });
        } else {
          setCheck({ ...check, email: 'incorrect' });
        }
        break;
      case 'password':
        if (inputValue.length >= minLength) {
          setCheck({ ...check, password: 'correct' });
        } else {
          setCheck({ ...check, password: 'incorrect' });
        }
        break;
      default:
        break;
    }
  };


  return (
    <form className="ContainerForm" method="POST" onSubmit={handleSubmit}>
      <h1>Registro de voluntario</h1>
      <FieldForm
        title={'Email'}
        type={'email'}
        name={'email'}
        onChange={(event) => {
          checkLength(event);
          setData({ ...data, email: event.target.value });
        }}
        stateValidate={check.email}
      />
      <FieldForm
        title={'Contraseña'}
        type={'password'}
        name={'password'}
        onChange={(event) => {
          checkLength(event);
          setData({ ...data, password: event.target.value });
        }}
        stateValidate={check.password}
      />
      <FieldForm
        title={'Repetir contraseña'}
        type={'password'}
        name={'repeatedPassword'}
        onChange={(event) => {
          checkLength(event);
          setData({ ...data, passwordRepeated: event.target.value });
        }}
        onBlur={checkPassword}
        stateValidate={check.passwordRepeated}
      />
      <SubmitButton text={'Enviar'}/>
      <p>
        ¿Ya tiene cuenta? <LinkText to={ROUTE.volunteer.login} text={'Iniciar sesión'}/>
      </p>
    </form>
  );
};

FormRegisterVolunteer.displayName = 'FormRegisterVolunteer';
