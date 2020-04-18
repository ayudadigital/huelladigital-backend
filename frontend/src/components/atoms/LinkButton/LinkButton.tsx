import { Link } from 'react-router-dom';
import React from 'react';
import './LinkButton.scss';

interface LinkButtonProps {
  path: string;
  text: string;
}

export const LinkButton: React.FC<LinkButtonProps> = ({path, text='boton'}) => {
  return (
    <Link to={path}>
      <button className={'LinkButton'}>{text}</button>
    </Link>
  );
};
