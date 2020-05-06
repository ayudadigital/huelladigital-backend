import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { InputFieldForm} from './';

describe('InputFieldForm', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <InputFieldForm type={'email'} name={'email'}/>,
    );
    expect(renderResult.queryByLabelText('input-form')).toBeTruthy();
  });
});
