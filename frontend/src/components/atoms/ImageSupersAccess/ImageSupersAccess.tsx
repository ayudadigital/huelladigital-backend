import * as React from 'react';
import './ImageSupersAccess.scss';
import superHeroes from './superHeroes.svg';

interface ImageSupersAccessProps {
  width?: number;
}

export const ImageSupersAccess: React.FC<ImageSupersAccessProps> = ({width}) => (
  <img src={superHeroes} alt="super heroes image" width={width}/>
);

ImageSupersAccess.displayName = 'ImageSupersAccess';
