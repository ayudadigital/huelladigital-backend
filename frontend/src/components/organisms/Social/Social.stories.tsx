import * as React from 'react';
import { Social } from './Social';
import { withA11y } from '@storybook/addon-a11y';

export default {
  title: 'Organisms | Social',
  decorators: [withA11y],
};

export const withText = () => {

  return (
    <div style={{ background: '#7E254E', height: '100vh', padding: '0', margin: '0' }}>
      <Social/>
    </div>
  );
};
