import * as React from 'react';
import './WrapperPages.scss';

export const WrapperPages: React.FC<{}> = ({children}) => (
  <div className="WrapperPages">
    {children}
  </div>
);

WrapperPages.displayName = 'WrapperPages';
