import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { FormRegisterOrganization } from './FormRegisterOrganization';

export default {
  title: 'Organisms | Forms ',
  decorators: [withA11y],
};

export const RegisterOrganization = () => <FormRegisterOrganization />;
