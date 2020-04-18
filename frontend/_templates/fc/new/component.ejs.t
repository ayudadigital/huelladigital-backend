---
to: src/<%= path %>/<%= name %>/<%= name %>.tsx
---
import * as React from 'react';
import './<%= name %>.scss';

export const <%= name %>: React.FC<{}> = () => (
  <div className="<%= name %>">
    Hello from <%= name %>!
  </div>
);

<%= name %>.displayName = '<%= name %>';
