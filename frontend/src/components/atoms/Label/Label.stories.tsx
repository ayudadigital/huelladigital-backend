import * as React from 'react';
import {Label} from './Label';
import { withA11y } from '@storybook/addon-a11y';

export default {
  title: 'Atom | Label',
  decorators: [withA11y],
};

export const withText = () => <Label text={'label component'}/>;
