import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { BrowserRouter } from 'react-router-dom';
import VolunteerLogin from './VolunteerLogin';


export default {
  title: 'Pages | Volunteer  ',
  decorators: [withA11y,
    (storyFn:any) => <BrowserRouter >{storyFn()}</BrowserRouter>
  ]
};

export const login = () => <VolunteerLogin/>;
