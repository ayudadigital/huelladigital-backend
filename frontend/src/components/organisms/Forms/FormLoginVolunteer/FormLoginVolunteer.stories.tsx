import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { FormLoginVolunteer } from './index';

export default {
  title: 'Organisms | Forms',
  decorators: [withA11y]
};


export const LoginVolunteer = () => <FormLoginVolunteer/>;
