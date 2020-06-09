import * as React from 'react';
import './Social.scss';
import { SocialNav } from '../../molecules/SocialNav';

export const Social: React.FC<{}> = () => (
  <div className="Social">
    <p>Síguenos</p>
    <SocialNav />
  </div>
);

Social.displayName = 'Social';
