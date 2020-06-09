import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { Politics} from './';

describe('Politics', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <Politics/>,
    );
    expect(renderResult.queryByText('Hello from Politics!')).toBeTruthy();
  });
});