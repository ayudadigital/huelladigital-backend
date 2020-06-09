import * as React from 'react';
import './Politics.scss';
import { LinkText } from '../../atoms/LinkText';

export const Politics: React.FC<{}> = () => (
  <ul className="Politics">
    <li>
      <LinkText to={'#'} text={'Aviso legal'} />
    </li>
    <li>
      <LinkText to={'#'} text={'Política de Privacidad'} />
    </li>
    <li>
      <LinkText to={'#'} text={'Política de Cookies'} />
    </li>
    <li>
      <LinkText to={'#'} text={'Preguntas Frecuentes'} />
    </li>
  </ul>
);

Politics.displayName = 'Politics';
