import React from 'react';
import { Switch, Route, HashRouter } from 'react-router-dom';
import Login from './login';
import ChooseGame from './chooseGame';
import SingleCardGame from './singleCardGame';

class Main extends React.Component {

    render() {
        return (
            <HashRouter>
                <Switch>
                    <Route path="/login" component={Login} />
                    <Route path="/chooseGame" component={ChooseGame} />
                    <Route path="/singleCardGame" component={SingleCardGame} />

                    <Route path="/" component={Login} />
                </Switch>
            </HashRouter>
        );
    }
}

export default Main;