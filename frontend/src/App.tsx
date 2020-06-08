import React from 'react';
import './styles/scss/index.scss';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import { Home } from './pages/Home/Home';
import { ROUTE } from './utils/routes';
import { EmailConfirmation } from './pages/EmailConfirmation';

const App: React.FC = () => {
  return (
    <div className="App">
      <Router>
        <Switch>
          <Route exact path={ROUTE.home}>
            <Home />
          </Route>
          <Route path={ROUTE.email.confirmation}>
            <EmailConfirmation/>
          </Route>
        </Switch>
      </Router>
    </div>
  );
};

export default App;
