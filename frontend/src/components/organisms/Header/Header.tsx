import * as React from 'react';
import './Header.scss';
import { Image } from '../../atoms/Image';
import { NavBar } from '../../molecules/NavBar';

export const Header: React.FC<{}> = () => (
  <div className="Header">
    <Image source="https://huellapositiva.com/wp-content/uploads/2020/03/cropped-Logo-Huella-Positiva-PV-05.png" description="logo"/>
    <NavBar/>
  </div>
);

Header.displayName = 'Header';
