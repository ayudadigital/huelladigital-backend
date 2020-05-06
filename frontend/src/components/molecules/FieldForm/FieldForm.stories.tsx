import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { FieldForm } from './FieldForm';

export default {
  title: 'Molecules | Field Form',
  decorators: [withA11y],
};

export const email = () => <FieldForm name={'text'} title={'Label'} type={'text'} value={'irrelevant text'}/>;
