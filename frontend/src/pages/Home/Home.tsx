import React from 'react';
import './styles.scss';
import { LinkButton } from '../../components/atoms/LinkButton/LinkButton';
import { ROUTE } from '../../utils/routes';
import { WrapperPages } from '../../components/templates/WrapperPages';


export const Home: React.FC<{}> = () => {
  return (
    <WrapperPages>
      <header/>
      <section className={'Home'}>
        <LinkButton path={ROUTE.volunteer.register} text={'Registrar voluntario'}/>
        <LinkButton path={ROUTE.volunteer.login} text={'Acceso voluntario'}/>
      </section>
      <footer/>
    </WrapperPages>
  );
};
