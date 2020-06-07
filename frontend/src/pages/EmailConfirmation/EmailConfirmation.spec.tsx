import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { EmailConfirmation } from './index';
import { BrowserRouter } from 'react-router-dom';

describe('EmailConfirmation', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <EmailConfirmation/>,
      </BrowserRouter>,
    );
    expect(renderResult.queryByText('reenviar correo', { exact: false })).toBeTruthy();
  });
});
