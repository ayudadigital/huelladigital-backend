import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { ImageSupersAccess} from './';

describe('ImageSupersAccess', () => {
  xit('should display the default message', () => {
    const renderResult: RenderResult = render(
      <ImageSupersAccess/>,
    );
    expect(renderResult.queryByText('Hello from ImageSupersAccess!')).toBeTruthy();
  });
});
