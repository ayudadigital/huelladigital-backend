import * as React from 'react';
import {NavBar} from './NavBar';
import { withA11y } from '@storybook/addon-a11y';
import { BrowserRouter } from 'react-router-dom';

export default {
  title: 'Molecules | NavBar',
  decorators: [withA11y,
    (storyFn:any) => <BrowserRouter>{storyFn()}</BrowserRouter>
    ],
};

export const Dashboard = () => {
  return (
    <div style={{background: '#7E254E', height: '100vh', padding: '0', margin: '0'}}>
    <NavBar/>
    </div>
  );
};
