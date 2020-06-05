import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { SubmitButton } from './SubmitButton';
import { withKnobs ,text} from '@storybook/addon-knobs';

export default {
  title: 'Atom | Submit Button',
  decorators: [withA11y, withKnobs],
};

export const withText = () => <SubmitButton text={text('TEXT', 'Button')}/>;
export const disabled = () => <SubmitButton text={text('TEXT', 'Button')} disabled={true}/>;
