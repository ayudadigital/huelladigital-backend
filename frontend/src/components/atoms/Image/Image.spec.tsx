import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { Image} from './';

describe('Image', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <Image source="#" description="url"/>,
    );
    expect(renderResult.queryByText('Hello from Image!')).toBeTruthy();
  });
});