import React from 'react';
import './styles.scss';
import { FormLoginVolunteer } from '../../components/organisms/Forms/FormLoginVolunteer';
import { Link } from 'react-router-dom';
import { ROUTE } from '../../utils/routes';

const VolunteerLogin: React.FC = () => {
  return (
    <div className="VolunteerLogin">
      <div className={'header'}>
        <Link to={ROUTE.home}> {'<- Inicio'}</Link>
        <Link to={ROUTE.volunteer.register}> {'Registrarse ->'}</Link>
      </div>

      <hr/>

      <div className="container">
        <FormLoginVolunteer/>
      </div>
    </div>
  );
};

export default VolunteerLogin;
