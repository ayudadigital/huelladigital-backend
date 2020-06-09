import React from 'react';
import './styles.scss';
import { WrapperPages } from '../../components/templates/WrapperPages';
import { MixForms } from '../../components/organisms/Forms/MixForms';
import { Footer } from '../../components/organisms/Footer';

export const Home: React.FC<{}> = () => {
  return (
    <WrapperPages>
      <header />
      <section className={'Home'}>
        <MixForms />
      </section>
      <Footer />
    </WrapperPages>
  );
};
