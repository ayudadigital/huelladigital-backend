import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { FormLoginVolunteer } from './index';

export default {
  title: 'Organisms | Form | Login volunteer ',
  decorators: [withA11y,
    (storyFn:()=>HTMLDivElement) => <div style={{ height: '60vh' }}>{storyFn()}</div>
  ],
};


export const LoginVolunteer = () => <FormLoginVolunteer/>;
