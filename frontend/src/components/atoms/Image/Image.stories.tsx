import * as React from 'react';
import {Image} from './Image';
import { withA11y } from '@storybook/addon-a11y';
import logo from '../../../../public/logo192.png';
export default {
  title: 'Atom | Image',
  decorators: [withA11y],
};

export const withText = () => <Image source={logo} description={"hola"}/>;
