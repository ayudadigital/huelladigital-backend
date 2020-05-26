import * as React from 'react';
import { BrowserRouter as Router, Switch, Route } from 'react-router-dom';
import { ROUTE } from '../../../../utils/routes';
import { FormRegisterOrganization } from '../FormRegisterOrganization/FormRegisterOrganization';
import { FormRegisterVolunteer } from '../FormRegisterVolunteer/FormRegisterVolunteer';
import { LinkText } from '../../../atoms/LinkText/LinkText';

import './styles.scss';

export const FormMixed: React.FC<{}> = () => (
  <div className="MixedFormRegister">
    <Router>
      <div className="MixedFormRegisterMenu">
        <LinkText to={ROUTE.volunteer.register} text={'QUIERO AYUDAR'} />
        <LinkText to={ROUTE.organization.register} text={'NECESITO AYUDA'} />
      </div>
      <Switch>
        <Route path={ROUTE.volunteer.register}>
          <FormRegisterVolunteer />
        </Route>
        <Route exact path={ROUTE.organization.register}>
          <FormRegisterOrganization />
        </Route>
      </Switch>
    </Router>
  </div>
);
