import * as React from 'react';
import { ImageSupersAccess } from './ImageSupersAccess';
import { withA11y } from '@storybook/addon-a11y';
import { Fragment } from 'react';

export default {
  title: 'Atom | Images',
  decorators: [withA11y],
};

export const SuperHeroesAccess = () => {
  return (
    <Fragment>
      <ImageSupersAccess width={400}/>
      <ImageSupersAccess width={200}/>
      <ImageSupersAccess width={100}/>
    </Fragment>
  );
};
