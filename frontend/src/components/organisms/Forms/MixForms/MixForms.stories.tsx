import * as React from 'react';
import { MixForms } from './MixForms';
import { withA11y } from '@storybook/addon-a11y';
import { BrowserRouter } from 'react-router-dom';

export default {
  title: 'Organisms | Forms ',
  decorators: [withA11y,
    (storyFn: any) => <BrowserRouter>{storyFn()}</BrowserRouter>,
  ],
};

export const mixForms = () => <MixForms/>;
