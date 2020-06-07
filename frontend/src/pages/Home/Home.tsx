import React from 'react';
import './styles.scss';
import { WrapperPages } from '../../components/templates/WrapperPages';
import { MixForms } from '../../components/organisms/Forms/mixForms';


export const Home: React.FC<{}> = () => {
  return (
    <WrapperPages>
      <header/>
      <section className={'Home'}>
        <MixForms/>
      </section>
      <footer/>
    </WrapperPages>
  );
};
