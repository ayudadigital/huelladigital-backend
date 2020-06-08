import * as React from 'react';
import { fireEvent, render, RenderResult } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { FormLoginVolunteer } from './FormLoginVolunteer';

const EMAIL = {
  allowed: 'irrelevant@email.com',
  wrong: 'wrongmail@email.',
};

const PASSWORD = {
  allowed: 'aLongPassword',
  wrong: 'short',
};

describe('FormLoginVolunteer should', () => {

  it('enable the button submit when all fields aren\'t empty', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <FormLoginVolunteer/>,
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
    expect(submitButton.disabled).toBe(false);
  });
});
