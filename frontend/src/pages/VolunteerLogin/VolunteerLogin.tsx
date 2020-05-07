import React from 'react';
import './styles.scss';
import { FormLoginVolunteer } from '../../components/organisms/Forms/FormLoginVolunteer';
import { ROUTE } from '../../utils/routes';
import { LinkText } from '../../components/atoms/LinkText';

const VolunteerLogin: React.FC = () => {
  return (
    <div className="VolunteerLogin">
      <div className={'header'}>
        <LinkText to={ROUTE.home} text={'Inicio'}/>
        <LinkText to={ROUTE.volunteer.register} text={'Registrarse'}/>
      </div>

      <hr/>

      <div className="container">
        <FormLoginVolunteer/>
      </div>
    </div>
  );
};

export default VolunteerLogin;
