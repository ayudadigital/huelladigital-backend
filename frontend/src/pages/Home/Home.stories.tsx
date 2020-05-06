import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { Home } from './Home';
import { BrowserRouter } from 'react-router-dom';


export default {
  title: 'Pages | Home ',
  decorators: [withA11y,
    (storyFn:any) => <BrowserRouter >{storyFn()}</BrowserRouter>
  ]
};

export const defaultView = () => <Home/>;
