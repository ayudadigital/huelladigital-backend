---
to: src/<%= path %>/<%= name %>/<%= name %>.stories.tsx
---
import * as React from 'react';
import {<%= name %>} from './<%= name %>';
import { withA11y } from '@storybook/addon-a11y';

export default {
  title: '<%= name %>',
  decorators: [withA11y],
};

export const withText = () => <<%= name %> />;
