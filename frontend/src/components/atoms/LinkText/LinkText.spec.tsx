import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { LinkText } from './';
import { BrowserRouter } from 'react-router-dom';

describe('LinkText', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <LinkText to={'/#'} text={'text-link'}/>,
      </BrowserRouter>,
    );
    expect(renderResult.queryByText('text-link')).toBeTruthy();
  });
});
