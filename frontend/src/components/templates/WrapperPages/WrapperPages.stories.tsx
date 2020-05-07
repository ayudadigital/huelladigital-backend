import * as React from 'react';
import {WrapperPages} from './WrapperPages';
import { withA11y } from '@storybook/addon-a11y';
import { CSSProperties } from 'react';

export default {
  title: 'WrapperPages',
  decorators: [withA11y],
};

const styles:CSSProperties = {
  background: 'grey',
  textAlign: 'center',
  padding:'2% 0',
};

export const Default = () => <WrapperPages>
  <header style={{...styles, background: 'lightgreen'}}>Header</header>
  <div style={{...styles, background: 'lightpink', padding:'5% 0'}}>Body</div>
  <footer style={{...styles, background: 'lightblue'}}>Footer</footer>
</WrapperPages>;
