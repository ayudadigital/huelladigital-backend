import { ROUTE } from '../../../../utils/routes';

export default class Client {
  registerVolunteer(credentials: object) {
    // FIXME: extract the URL to a variable on ENV i guess?
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
          // FIXME: extract the URL to a variable on ENV i guess?
          window.location.replace(`http://localhost:3000${ROUTE.email.confirmation}`);
        }
      })
      // tslint:disable-next-line:no-console
      .catch((error) => console.log(error));
  }
}
