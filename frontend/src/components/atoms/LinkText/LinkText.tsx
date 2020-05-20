import * as React from 'react';
import './styles.scss';
import { Link } from 'react-router-dom';
import { LinkTextProps } from './types';

export const LinkText: React.FC<LinkTextProps> = ({to, text}) => (
  <Link className="LinkText" to={to}>{text}</Link>
);

LinkText.displayName = 'LinkText';
