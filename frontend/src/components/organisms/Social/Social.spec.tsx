import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { Social} from './';

describe('Social', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <Social/>,
    );
    expect(renderResult.queryByText('Hello from Social!')).toBeTruthy();
  });
});