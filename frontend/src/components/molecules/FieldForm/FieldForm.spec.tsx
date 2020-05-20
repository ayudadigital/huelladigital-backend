import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { FieldForm } from './FieldForm';

describe('FieldForm', () => {
  xit('should display the default message', () => {
    const textValue = 'irrelevant text';
    const renderResult: RenderResult = render(
      <FieldForm type={'text'} name={'text'} title={'text'} value={textValue}/>,
    );
    expect(renderResult.queryByLabelText('input-form')).toBeTruthy();
  });
});
