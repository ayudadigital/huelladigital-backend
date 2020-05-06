import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { SubmitButton } from './SubmitButton';

export default {
  title: 'Atom | Submit Button',
  decorators: [withA11y],
};

export const withText = () => <SubmitButton text={'Button'}/>;
