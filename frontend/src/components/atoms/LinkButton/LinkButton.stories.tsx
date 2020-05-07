import * as React from 'react';
import { withA11y } from '@storybook/addon-a11y';
import { withKnobs ,text} from '@storybook/addon-knobs';
import { LinkButton } from './LinkButton';
import { BrowserRouter } from 'react-router-dom';

export default {
  title: 'Atom | Link Button',
  decorators: [withA11y, withKnobs,
    (storyFn:any) => <BrowserRouter>{storyFn()}</BrowserRouter>
  ],
};

export const withText = () => <LinkButton text={text('TEXT', 'Button', 'button-link')}
                                          path={text('LINK TO', '/#', 'button-link')}/>;

