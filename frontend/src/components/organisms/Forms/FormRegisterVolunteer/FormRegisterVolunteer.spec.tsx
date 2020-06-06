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
  wrong: 'short'
};

describe('FormRegisterVolunteer', () => {
  it('should disabled the button submit when have any part of the form is wrong', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <FormRegisterVolunteer/>,
      </BrowserRouter>,
    );
    const submitButton = renderResult.queryByLabelText('submit-button');
    expect(submitButton.disabled).toBe(true);
  });

  it('should enable the button submit when all fields are fill with allowed values', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <FormRegisterVolunteer/>,
      </BrowserRouter>,
    );
    const submitButton = renderResult.queryByLabelText('submit-button');

    const emailField = renderResult.queryAllByLabelText('input-form')[0];
    expect(emailField.name).toContain('email');
    expect(emailField.value).toContain('');
    fireEvent.change(emailField, {
      target:
        {
          value: EMAIL.allowed,
        },
    });
    expect(emailField.value).toContain(EMAIL.allowed);
    expect(submitButton.disabled).toBe(true);

    const passwordField = renderResult.queryAllByLabelText('input-form')[1];
    expect(passwordField.name).toContain('password');
    expect(passwordField.value).toContain('');
    fireEvent.change(passwordField, {
      target:
        {
          value: PASSWORD.allowed,
        },
    });
    expect(passwordField.value).toContain(PASSWORD.allowed);
    expect(submitButton.disabled).toBe(true);

    const repeatPasswordField = renderResult.queryAllByLabelText('input-form')[2];
    expect(repeatPasswordField.name).toContain('repeatedPassword');
    expect(repeatPasswordField.value).toContain('');
    fireEvent.change(repeatPasswordField, {
      target:
        {
          value: PASSWORD.allowed,
        },
    });
    expect(repeatPasswordField.value).toContain(PASSWORD.allowed);
    expect(submitButton.disabled).toBe(false);
  });
});
