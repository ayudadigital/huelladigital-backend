
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
        if (response.status === 201) {
          // tslint:disable-next-line:no-console
          console.log('TODO CORRECTO');
        }
      })
      // tslint:disable-next-line:no-console
      .catch((error) => console.log(error));
  }
}
