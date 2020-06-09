import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { Footer} from './';

describe('Footer', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <Footer/>,
    );
    expect(renderResult.queryByText('Hello from Footer!')).toBeTruthy();
  });
});