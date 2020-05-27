import React, { useState } from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';

export const FormRegisterVolunteer: React.FC<{}> = () => {
  const [email, setemail] = useState('');
  const [password, setpassword] = useState('');
  const [repeatedPassword, setrepeatedPassword] = useState('');
  const getDataUser = (data: any) => {
    const inputTypeIsEmail = data[0] === 'email';
    const inputTypeIsPassword = data[0] === 'password';
    const inputTypeIsRepeatedPassword = data[0] === 'repeatedPassword';
    const valueInput = data[1];
    if (inputTypeIsEmail) setemail(valueInput);
    if (inputTypeIsPassword) setpassword(valueInput);
    if (inputTypeIsRepeatedPassword) setrepeatedPassword(valueInput);
  };
  const handleSubmit = (event: any) => {
    event.preventDefault();
    const URL = 'http://localhost:8080/api/v1/volunteers';
    const data = {
      email: email,
      password: password,
    };
    fetch(URL, {
      method: 'POST',
      body: JSON.stringify(data),
      headers: {
        'Content-Type': 'application/json',
      },
    })
      .then((res) => console.log(res))
      .catch((error) => console.error('Error:', error))
      .then((response) => console.log('Success:', response));
  };
  return (
    <form className="ContainerForm" method="POST" onSubmit={handleSubmit}>
      <h1>Registro de voluntario</h1>
      <FieldForm title={'Email'} type={'email'} name={'email'} getData={getDataUser} />
      <FieldForm
        title={'Contraseña'}
        type={'password'}
        name={'password'}
        getData={getDataUser}
      />
      <FieldForm
        title={'Repetir contraseña'}
        type={'password'}
        name={'repeatedPassword'}
        getData={getDataUser}
      />
      <label htmlFor="privacity">
        <input type="checkbox" name="privacity" />
        Acepto la política de privacidad del sitio.
      </label>
      <SubmitButton text={'Acceder'} />
      <p>¿Ya tiene cuenta? Iniciar sesión</p>
    </form>
  );
};

FormRegisterVolunteer.displayName = 'FormRegisterVolunteer';
