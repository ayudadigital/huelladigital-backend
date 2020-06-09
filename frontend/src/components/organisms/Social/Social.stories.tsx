import * as React from 'react';
import {Social} from './Social';
import { withA11y } from '@storybook/addon-a11y';

export default {
  title: 'Social',
  decorators: [withA11y],
};

export const withText = () => <Social />;
