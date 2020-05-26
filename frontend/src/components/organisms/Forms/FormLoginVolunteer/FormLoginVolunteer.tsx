import React, { useState, useEffect } from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';

export const FormLoginVolunteer: React.FC<{}> = () => {
  const [email, setemail] = useState('');
  const [password, setpassword] = useState('');

  const getDataUser = (data: any) => {
    const inputTypeIsEmail = data[0] === 'email';
    const inputTypeIsPassword = data[0] === 'password';
    const valueInput = data[1];
    if (inputTypeIsEmail) setemail(valueInput);
    if (inputTypeIsPassword) setpassword(valueInput);
  };

  const URL = 'http://localhost:8080/api/v1/volunteers';
  //const URL = 'https://webhook.site/63587131-e3bb-487e-857c-6fac629f0894';
  /*
  useEffect(() => {
    fetch(URL, {
      method: 'POST',
      mode: 'no-cors',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email: email, password: password }),
    })
      .then((response) => response.json())
      .then((response) => console.log(response));
  }, []); */

  const handleSubmit = () => {
    fetch(URL, {
      method: 'POST',
      body: JSON.parse(`{ "email": ${email} }`),
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
    })
      .then((response) => response.json())
      .then((response) => {
        if (response.status === 'success') {
          alert('Message Sent.');
        } else if (response.status === 'fail') {
          alert('Message failed to send.');
        }
      });
  };

  return (
    <form className="ContainerForm" method="POST" id="form" onSubmit={handleSubmit}>
      <h1>Acceso de voluntario</h1>
      <FieldForm title={'Email'} type={'email'} name={'email'} getData={getDataUser} />
      <FieldForm
        title={'Contraseña'}
        type={'password'}
        name={'password'}
        getData={getDataUser}
      />

      <SubmitButton text={'Acceder'} />
      <p>¿Ya tiene cuenta? Iniciar sesión</p>
      <p>{email}</p>
      <p>{password}</p>
    </form>
  );
};

FormLoginVolunteer.displayName = 'FormLoginVolunteer';
