import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { NavBar} from './';

describe('NavBar', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <NavBar/>,
    );
    expect(renderResult.queryByText('Hello from NavBar!')).toBeTruthy();
  });
});