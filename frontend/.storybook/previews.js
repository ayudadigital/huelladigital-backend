import React from 'react';
import { addDecorator } from '@storybook/react';


addDecorator(storyFn => <div
  style={{ fontFamily: 'Segoe UI, Roboto, Oxygen, Ubuntu , Cantarell, Fira Sans, Droid Sans, Helvetica Neue' }}>{storyFn()}</div>);
