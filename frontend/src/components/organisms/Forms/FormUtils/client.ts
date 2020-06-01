export default class Client {
  registerVolunteer(credentials: any) {
    const URL = 'http://localhost:8080/api/v1/volunteers';
    fetch(URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    })
      .then((response) => {
        if (response.status === 201) console.log('TODO CORRECTO');
      })
      .catch((error) => console.log(error));
  }
}
