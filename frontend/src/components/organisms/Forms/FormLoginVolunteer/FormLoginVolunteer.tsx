import React, { useEffect, useState } from 'react';
import '../styles.scss';
import { FieldForm } from '../../../molecules/FieldForm';
import { SubmitButton } from '../../../atoms/SubmitButton';

export const FormLoginVolunteer: React.FC<{}> = () => {
  const [stateButton, setStateButton] = useState(true);
  const [data, setData] = useState(
    {
      email: '',
      password: '',
    },
  );

  function handleStateButton() {
    (data.email !== '' && data.password !== '') ? setStateButton(false)
      : setStateButton(true);
  }

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();

    // TODO: Add here all the logic when user login
  }

  useEffect(() => {
    handleStateButton();
    // eslint-disable-next-line
  }, [data]);

  return (
    <form className="ContainerForm" method="POST" id="form" onSubmit={handleSubmit}>
      <FieldForm title={'Email'}
                 type={'email'}
                 name={'email'}
                 onChange={(e) => setData({ ...data, email: e.target.value })}/>
      <FieldForm title={'Contraseña'}
                 type={'password'}
                 name={'password'}
                 onChange={(e) => setData({ ...data, password: e.target.value })}/>
      <SubmitButton text={'Acceder'} disabled={stateButton}/>
    </form>
  );
};

FormLoginVolunteer.displayName = 'FormLoginVolunteer';
