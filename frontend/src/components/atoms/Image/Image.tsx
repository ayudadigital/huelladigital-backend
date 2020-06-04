import * as React from 'react';
import './Image.scss';

interface ImageProps {
  source:string,
  description: string
} 

export const Image: React.FC<ImageProps> = ({source, description}) => (
  <img src={source} alt={description}/>
);

Image.displayName = 'Image';
