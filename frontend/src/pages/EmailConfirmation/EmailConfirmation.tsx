import * as React from 'react';
import './EmailConfirmation.scss';
import { LinkText } from '../../components/atoms/LinkText';
import { Link } from 'react-router-dom';

export const EmailConfirmation: React.FC<{}> = () => (
  <div className="EmailConfirmation">
    <h2>Gracias por unirte a la causa de Huella Positiva</h2>
    <p>En unos minutos deberías de haber recibido un <strong>correo de confirmación para verificar tu email</strong></p>

    <div className={'resend-container'}>
    <p>Si no has recibido ningún correo tras 5 min. puedes probar a mandarlo de nuevo pinchando en <LinkText to={'/'} text={'reenviar correo'}/></p>
    </div>

      <br/>
      <br/>
      <br/>
      <Link to={'/'}>
      Volver al inicio
      </Link>
  </div>
);

EmailConfirmation.displayName = 'EmailConfirmation';
