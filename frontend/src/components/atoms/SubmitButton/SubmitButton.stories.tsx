import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { SubmitButton } from './SubmitButton';
import { withKnobs ,text} from '@storybook/addon-knobs';
import { BrowserRouter } from 'react-router-dom';

export default {
  title: 'Atom | Submit Button',
  decorators: [withA11y, withKnobs,
    (storyFn:any) => <BrowserRouter>{storyFn()}</BrowserRouter>
  ],
};

export const withText = () => <SubmitButton text={text('TEXT', 'Button')}/>;
export const disabled = () => <SubmitButton text={text('TEXT', 'Button')} disabled={true}/>;
