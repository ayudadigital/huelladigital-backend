import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { LinkButton } from './LinkButton';
import { BrowserRouter } from 'react-router-dom';

describe('Link Button', () => {
  it('should display the default message', () => {
    const text = 'button';
    const renderResult: RenderResult = render(
      <BrowserRouter>
        <LinkButton text={text} path={'/#'}/>,
      </BrowserRouter>,
    );
    expect(renderResult.queryByText(text)).toBeTruthy();
  });
});
