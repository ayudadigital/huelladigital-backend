---
to: src/<%= path %>/<%= name %>/<%= name %>.spec.tsx
---
import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { <%= name %>} from './<% name %>';

describe('<%= name %>', () => {
  it('should display the default message', () => {
    const renderResult: RenderResult = render(
      <<%= name %>/>,
    );
    expect(renderResult.queryByText('Hello from <%= name %>!')).toBeTruthy();
  });
});