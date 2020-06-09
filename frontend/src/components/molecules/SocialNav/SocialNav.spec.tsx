import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { SocialNav} from './';

describe('SocialNav', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <SocialNav/>,
    );
    expect(renderResult.queryByText('Hello from SocialNav!')).toBeTruthy();
  });
});