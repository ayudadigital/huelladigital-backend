import React, { useState } from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';

export const FormRegisterVolunteer: React.FC<{}> = () => {

  const [email, setemail] = useState('');
  const [password, setpassword] = useState('');
  const [repeatedPassword, setrepeatedPassword] = useState('');

  // Obtener datos de los inputs
  const getDataUser = (data: any) => {
    const inputTypeIsEmail = data[0] === 'email';
    const inputTypeIsPassword = data[0] === 'password';
    const inputTypeIsRepeatedPassword = data[0] === 'repeatedPassword';
    const valueInput = data[1];
    if (inputTypeIsEmail) setemail(valueInput);
    if (inputTypeIsPassword) setpassword(valueInput);
    if (inputTypeIsRepeatedPassword) setrepeatedPassword(valueInput);

    // Llamada a checkPassWord
    checkPassword(password, repeatedPassword)
  };

  // Comprobar contraseñas 
  const checkPassword = (pass: String, passrepeated: String) => {
    // Requisitos contraseñas
    console.log("Hola")
    // 1- Mayor de 6 y menor de 20
    if (checkLength(pass)) {
      console.log("añadir color verde")
    }
    else {
      console.log("añadir color rojo")
    }
    // 2- Mínimo 1 mayúscula y 1 minúscula
    // 3- Mínimo 1 caracter raro (~! @ # $% ^& * -+ = ' | \ \ (){}\ []:; "' <>,.? /)
    // 4- Mínimo 1 número

  }

  const checkLength = (password: String) => {
    const minLenght: number = 5
    const maxLenght: number = 19
    let comprobar: boolean = false
    if (password.length >= minLenght && password.length <= maxLenght)
      comprobar = true;
    return comprobar
  }

  // Conexión back-front
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

      <p>{password}</p>
      <p>{repeatedPassword}</p>
    </form>
  );
};

FormRegisterVolunteer.displayName = 'FormRegisterVolunteer';
