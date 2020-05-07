import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { WrapperPages} from './';

describe('WrapperPages', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <WrapperPages/>,
    );
    expect(renderResult.queryByText('Hello from WrapperPages!')).toBeTruthy();
  });
});