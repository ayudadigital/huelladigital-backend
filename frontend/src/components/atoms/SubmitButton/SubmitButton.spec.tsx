import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { SubmitButton } from './SubmitButton';

describe('Submit Button', () => {
  it('should display the default message', () => {
    const text = 'button';
    const renderResult: RenderResult = render(
      <SubmitButton text={text}/>,
    );
    expect(renderResult.queryByText(text)).toBeTruthy();
  });
});
