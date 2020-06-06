import * as React from 'react';
import './MixForms.scss';
import { FormRegisterVolunteer } from '../FormRegisterVolunteer';
import { FormLoginVolunteer } from '../FormLoginVolunteer';
import { useState } from 'react';

export const MixForms: React.FC<{}> = () => {
  const [focus, setFocus] = useState(true);
  const [stateButton, setStateButton] = useState(false);

  const handleFocus = () => {
    setFocus(!focus);
    setStateButton(!stateButton);
  };

  return (
    <div className="mixForms">
      <div className={'focusButton'}>
        <button aria-label={'login-button'} onClick={handleFocus} disabled={!stateButton}>Iniciar sesión</button>
        <button aria-label={'register-button'} onClick={handleFocus} disabled={stateButton}>Registrarse</button>
      </div>
      {
        focus ? <FormLoginVolunteer/> : <FormRegisterVolunteer/>
      }
    </div>
  );
};

MixForms.displayName = 'mixForms';
