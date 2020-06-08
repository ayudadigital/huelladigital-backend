import * as React from 'react';
import { Image } from './Image';
import { withA11y } from '@storybook/addon-a11y';
import logo from '../../../../public/logo192.png';
import superHeroes from './assets/superHeroes.svg';
import { Fragment } from 'react';
import { text, withKnobs } from '@storybook/addon-knobs';

export default {
  title: 'Atom | Image',
  decorators: [withA11y, withKnobs],
};

export const Logo = () => <Image source={logo} description={'logo'} width={text('Ancho', '100px')}/>;
export const SuperHeroes = () => {
  return (
    <Fragment>
      <Image source={superHeroes} description={'super heroes logo register'}/>
      <Image source={superHeroes} description={'super heroes logo register'} width={'300px'}/>
      <Image source={superHeroes} description={'super heroes logo register'} width={'10%'}/>
    </Fragment>

  );
};
