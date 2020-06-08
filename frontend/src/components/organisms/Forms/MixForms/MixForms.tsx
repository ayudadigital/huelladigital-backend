import * as React from 'react';
import { useState } from 'react';
import './MixForms.scss';
import { FormRegisterVolunteer } from '../FormRegisterVolunteer';
import { FormLoginVolunteer } from '../FormLoginVolunteer';

export const MixForms: React.FC<{}> = () => {
  const [activeLogin, setActiveLogin] = useState(true);
  const [stateButton, setStateButton] = useState(false);

  const handleFocus = () => {
    setActiveLogin(!activeLogin);
    setStateButton(!stateButton);
  };

  return (
    <div className="mixForms">
      <div className={'focusButton'}>
        <button aria-label={'login-button'} onClick={handleFocus} disabled={!stateButton}>Iniciar sesión</button>
        <button aria-label={'register-button'} onClick={handleFocus} disabled={stateButton}>Registrarse</button>
      </div>
      {
        activeLogin ? <FormLoginVolunteer/> : <FormRegisterVolunteer/>
      }
    </div>
  );
};

MixForms.displayName = 'mixForms';
