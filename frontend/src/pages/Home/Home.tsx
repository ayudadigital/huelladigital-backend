import React from 'react';
import './styles.scss';
import { WrapperPages } from '../../components/templates/WrapperPages';
import { MixForms } from '../../components/organisms/Forms/MixForms';
import register from './assets/register.svg';

const bgStyle = {
  background: `url(${register}) no-repeat top center`,
  backgroundSize: '100%',
};

export const Home: React.FC<{}> = () => {
  return (
    <WrapperPages>
      <header />
      <section className={'Home'} style={bgStyle}>
        <MixForms />
      </section>
      <footer />
    </WrapperPages>
  );
};
