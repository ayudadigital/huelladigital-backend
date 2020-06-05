import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { EmailConfirmation} from './index';

describe('EmailConfirmation', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <EmailConfirmation/>,
    );
    expect(renderResult.queryByText('Hello from EmailConfirmation!')).toBeTruthy();
  });
});
