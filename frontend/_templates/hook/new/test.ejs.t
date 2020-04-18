---
to: src/hooks/<%= name %>/<%= name %>.spec.ts
---
import { act, renderHook } from '@testing-library/react-hooks';
import { <%= name %> } from './<%= name %>';

describe('<%= name %>', () => {
  it('should display the default message', () => {
    const { result } = renderHook(() => <%= name %>());
    expect(result).toBeTruthy();
  });
});
