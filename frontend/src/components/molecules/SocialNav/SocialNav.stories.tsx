import * as React from 'react';
import {SocialNav} from './SocialNav';
import { withA11y } from '@storybook/addon-a11y';

export default {
  title: 'Molecules | SocialNav',
  decorators: [withA11y],
};

export const White = () => {
  return (
    <div style={{background: '#7E254E', height: '100vh', padding: '0', margin: '0'}}>
    <SocialNav/>
    </div>
  );
};
