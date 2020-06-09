import * as React from 'react';
import './Footer.scss';
import bannerER from '../../../assets/banner-er.png';
import bannerOdds from '../../../assets/banner-odds.png';
import { Politics } from '../../molecules/Politics';
import { Social } from '../Social';
import waveFooter from './assets/wave-footer.svg';

const bgStyle = {
  background: `url(${waveFooter}) top center no-repeat, #7e254e`,
  backgroundSize: '110%, auto',
};

export const Footer: React.FC<{}> = () => (
  <div className="Footer" style={bgStyle}>
    <Social />
    <img src={bannerER} alt="IMAGEN" />
    <img src={bannerOdds} alt="IMG" />
    <Politics />
    <p>Huella Positiva © 2020 Huella Positiva</p>
  </div>
);

Footer.displayName = 'Footer';
