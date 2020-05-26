import React from 'react';
import '../styles.scss';
import axios from 'axios';


export const FormLoginVolunteer: React.FC<{}> = () => {

  const getFormData = (event: any) => {
    event.preventDefault();
  };

  const submitData = (event: any) => {
    event.preventDefault();

    const data = {
      email: "airandkjhasdk@gmail.com",
      password: "asdasdasd"
    }
    /*
    fetch('http://localhost:8080/api/v1/volunteers', {
      method: 'POST', // or 'PUT'
      body: JSON.stringify(data), // data can be `string` or {object}!
      headers: {
        'Content-Type': 'application/json'
      },
      mode: "no-cors"
    }).then(res => console.log(res))
      .catch(error => console.error('Error:', error))
      .then(response => console.log('Success:', response));
  };
*/


    axios({
      method: 'post',
      url: 'http://localhost:8080/api/v1/volunteers',
      headers: {
        "Content-Type": "application/json",
        'Access-Control-Allow-Origin': false
      },
      data: JSON.stringify(data)
    })
      .then(response => {
        console.log("Hola")
        console.log(response);
      })
      .catch(err => console.log(err));

  }
  return (
    <form className="ContainerForm" method="POST" id="form" onSubmit={submitData}>
      <h1>Acceso de voluntario</h1>
      <input type="text" name="Correo" onChange={getFormData} />
      <input type="password" name="Password" onChange={getFormData} />
      <button type="submit">Enviar</button>


    </form>
  );
};

FormLoginVolunteer.displayName = 'FormLoginVolunteer';
