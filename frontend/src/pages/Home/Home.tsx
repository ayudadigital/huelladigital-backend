import React from 'react';
import './Home.scss';
import { LinkButton } from '../../components/atoms/LinkButton/LinkButton';
import { ROUTE } from '../../utils/routes';


export const Home: React.FC<{}> = () => {
  return (
    <div className={'Home'}>
      <LinkButton path={ROUTE.volunteer.register} text={'Registrar voluntario'}/>
      <LinkButton path={ROUTE.volunteer.login} text={'Acceso voluntario'}/>
    </div>
  );
};
