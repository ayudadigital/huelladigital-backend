import * as React from 'react';
import {Label} from './Label';
import { withA11y } from '@storybook/addon-a11y';
import { withKnobs, text } from '@storybook/addon-knobs';

export default {
  title: 'Atom | Label',
  decorators: [withA11y, withKnobs],
};

export const withText = () => <Label text={text('LABEL', 'Hola! soy el componente label')}/>;
