import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { BrowserRouter } from 'react-router-dom';
import VolunteerRegister from './VolunteerRegister';


export default {
  title: 'Pages | Volunteer  ',
  decorators: [withA11y,
    (storyFn:any) => <BrowserRouter >{storyFn()}</BrowserRouter>
  ]
};

export const register = () => <VolunteerRegister/>;
