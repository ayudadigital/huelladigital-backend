import * as React from 'react';
import './Image.scss';

interface ImageProps {
  source:string;
  description: string;
  width?: string;
}

export const Image: React.FC<ImageProps> = ({source, description= 'image', width}) => (
  <img src={source} alt={description} width={width}/>
);

Image.displayName = 'Image';
