import * as React from 'react';
import {Header} from './Header';
import { withA11y } from '@storybook/addon-a11y';
import { BrowserRouter } from 'react-router-dom';

export default {
  title: 'Organisms | Header',
  decorators: [withA11y,
    (storyFn:any) => <BrowserRouter>{storyFn()}</BrowserRouter>
    ],
};

export const withText = () => <Header />;
