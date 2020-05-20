import * as React from 'react';
import {LinkText} from './LinkText';
import { withA11y } from '@storybook/addon-a11y';
import { BrowserRouter } from 'react-router-dom';
import { text } from '@storybook/addon-knobs';

export default {
  title: 'Atom | LinkText',
  decorators: [withA11y,
  (storyFn:any) => <BrowserRouter>{storyFn()}</BrowserRouter>
  ],
};

export const withText = () => <LinkText to={text('LINK TO', '/#', 'text-link')}
                                        text={text('TEXT', 'Link to another page', 'text-link')}/>;
