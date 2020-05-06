import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { FormRegisterVolunteer } from './FormRegisterVolunteer';

export default {
  title: 'Organisms | Forms ',
  decorators: [withA11y,
    (storyFn:()=>HTMLDivElement) => <div style={{ height: '80vh' }}>{storyFn()}</div>
  ],
};

export const RegisterVolunteer = () => <FormRegisterVolunteer/>;
