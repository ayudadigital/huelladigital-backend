import * as React from 'react';
import {InputFieldForm} from './InputFieldForm';
import { withA11y } from '@storybook/addon-a11y';

export default {
  title: 'Atom | InputFieldForm',
  decorators: [withA11y],
};

export const text = () => <InputFieldForm  name={'email'} type={'text'} value={'Jhon Doe'}/>;
export const email = () => <InputFieldForm  name={'email'} type={'email'} value={'jhondoe@email.com'}/>;
export const password = () => <InputFieldForm  name={'password'} type={'password'} value={'password'}/>;
