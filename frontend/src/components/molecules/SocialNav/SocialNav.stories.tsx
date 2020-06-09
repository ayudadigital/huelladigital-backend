import * as React from 'react';
import {SocialNav} from './SocialNav';
import { withA11y } from '@storybook/addon-a11y';

export default {
  title: 'SocialNav',
  decorators: [withA11y],
};

export const withText = () => <SocialNav />;
