import React from 'react';
import './styles.scss';
import { FormRegisterVolunteer } from '../../components/organisms/Forms/FormRegisterVolunteer';
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
