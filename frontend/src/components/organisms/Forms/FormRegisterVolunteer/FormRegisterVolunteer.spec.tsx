import * as React from 'react';
import { FormRegisterVolunteer } from './FormRegisterVolunteer';
import { render, RenderResult, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';

const EMAIL = {
  allowed: 'irrelevant@email.com',
  wrong: 'wrongmail@email.',
};

const PASSWORD = {
  allowed: 'aLongPassword',
  wrong: 'short',
};

describe('FormRegisterVolunteer should', () => {
  it('disabled the button submit when have any part of the form is wrong', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <FormRegisterVolunteer/>,
      </BrowserRouter>,
    );
    const submitButton = renderResult.queryByLabelText('submit-button');
    // @ts-ignore
    expect(submitButton.disabled).toBe(true);
  });

  it('display a message when email are not allowed or is wrong', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <FormRegisterVolunteer/>,
      </BrowserRouter>,
    );

    const emailField = renderResult.queryAllByLabelText('input-form')[0];
    fireEvent.change(emailField, { target: { value: EMAIL.wrong } });
    expect(renderResult.queryByText('El email introducido no es válido')).toBeTruthy();
  });

  it('display a message when password is too short', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <FormRegisterVolunteer/>,
      </BrowserRouter>,
    );

    const passwordField = renderResult.queryAllByLabelText('input-form')[1];
    fireEvent.change(passwordField, { target: { value: PASSWORD.wrong } });
    expect(renderResult.queryByText('Contraseña demasiado corta', {exact:false})).toBeTruthy();
  });

  it('display a message when repeat password is not the same', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <FormRegisterVolunteer/>,
      </BrowserRouter>,
    );

    const passwordField = renderResult.queryAllByLabelText('input-form')[1];
    fireEvent.change(passwordField, { target: { value: PASSWORD.allowed } });

    const repeatPasswordField = renderResult.queryAllByLabelText('input-form')[2];
    fireEvent.change(repeatPasswordField, { target: { value: PASSWORD.wrong } });

    expect(renderResult.queryByText('Las contraseñas no coinciden')).toBeTruthy();
  });

  it('enable the button submit when all fields are fill with allowed values', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <FormRegisterVolunteer/>,
      </BrowserRouter>,
    );
    const submitButton = renderResult.queryByLabelText('submit-button');

    const emailField = renderResult.queryAllByLabelText('input-form')[0];
    fireEvent.change(emailField, { target: { value: EMAIL.allowed } });
    // @ts-ignore
    expect(emailField.value).toContain(EMAIL.allowed);
    // @ts-ignore
    expect(submitButton.disabled).toBe(true);

    const passwordField = renderResult.queryAllByLabelText('input-form')[1];
    fireEvent.change(passwordField, { target: { value: PASSWORD.allowed } });
    // @ts-ignore
    expect(passwordField.value).toContain(PASSWORD.allowed);
    // @ts-ignore
    expect(submitButton.disabled).toBe(true);

    const repeatPasswordField = renderResult.queryAllByLabelText('input-form')[2];
    fireEvent.change(repeatPasswordField, { target: { value: PASSWORD.allowed } });
    // @ts-ignore
    expect(repeatPasswordField.value).toContain(PASSWORD.allowed);
    // @ts-ignore
    expect(submitButton.disabled).toBe(false);
  });
});
