import * as React from 'react';
import {EmailConfirmation} from './EmailConfirmation';
import { withA11y } from '@storybook/addon-a11y';
import { BrowserRouter } from 'react-router-dom';

export default {
  title: 'Pages | EmailConfirmation',
  decorators: [withA11y,
    (storyFn:any) => <BrowserRouter >{storyFn()}</BrowserRouter>
  ],
};

export const Default = () => <EmailConfirmation />;
