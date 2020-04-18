import React from 'react';
import './index.scss';
import { BrowserRouter as Router, Switch, Route } from 'react-router-dom';
import VolunteerRegister from './pages/VolunteerRegister/VolunteerRegister';
import VolunteerLogin from './pages/VolunteerLogin/VolunteerLogin';
import { Home } from './pages/Home/Home';
import { ROUTE } from './utils/routes';

const App: React.FC = () => {
  return (
    <div className="App">
      <Router>
        <section className="section">
          <div className="container">
            <Switch>
              <Route exact path={ROUTE.home}>
                <Home/>
              </Route>
              <Route exact path={ROUTE.volunteer.login}>
                <VolunteerLogin/>
              </Route>
              <Route path={ROUTE.volunteer.register}>
                <VolunteerRegister/>
              </Route>
            </Switch>
          </div>
        </section>
      </Router>
    </div>
  );
};

export default App;
