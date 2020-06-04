import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { Header} from './';

describe('Header', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <Header/>,
    );
    expect(renderResult.queryByText('Hello from Header!')).toBeTruthy();
  });
});