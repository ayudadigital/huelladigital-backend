import * as React from 'react';
import './Label.scss';
import { LabelProps } from './types';


export const Label: React.FC<LabelProps> = ({text}) => (
  <label className="Label">{text}</label>
);




Label.displayName = 'Label';
