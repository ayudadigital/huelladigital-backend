import * as React from 'react';
import { fireEvent, render, RenderResult } from '@testing-library/react';
import { MixForms } from './';
import { BrowserRouter } from 'react-router-dom';

describe('MixForms should', () => {
  it('show the login session first', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <MixForms/>,
      </BrowserRouter>,
    );
    expect(renderResult.queryByText('Repetir contraseña')).toBeFalsy();
  });

  it('show the registration', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <MixForms/>,
      </BrowserRouter>,
    );

    const registerButton = renderResult.queryByLabelText('register-button');
    // @ts-ignore
    fireEvent.click(registerButton);
    expect(renderResult.queryByText('Repetir contraseña')).toBeTruthy();
  });
});
