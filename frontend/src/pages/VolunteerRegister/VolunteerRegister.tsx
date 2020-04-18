import React from 'react';
import './VolunteerRegister.scss';
import { FormRegisterVolunteer } from '../../components/molecules/FormRegisterVolunteer';
import { Link } from 'react-router-dom';
import { ROUTE } from '../../utils/routes';

const VolunteerRegister: React.FC = () => {
  return (
    <div className="VolunteerRegister">
      <div className={'header'}>
        <Link to={ROUTE.home}> {'<- Inicio'}</Link>
        <Link to={ROUTE.volunteer.login}> {'Acceder ->'}</Link>
      </div>

      <hr/>

      <div className="container">
        <FormRegisterVolunteer/>
      </div>
    </div>
  );
};

export default VolunteerRegister;
