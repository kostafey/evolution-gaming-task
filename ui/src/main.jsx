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
                    <Route path="/login" render={(props) =>
                      <Login {...props}
                            context={this.context}
                            parent={this}/>} />
                    <Route path="/chooseGame" render={(props) =>
                      <ChooseGame {...props}
                            context={this.context}
                            parent={this}/>} />
                    <Route path="/singleCardGame" render={(props) =>
                            <SingleCardGame {...props}
                                  context={this.context}
                                  parent={this}/>}/>

                    <Route path="/" render={(props) =>
                        <Login {...props}
                                context={this.context}
                                parent={this}/>} />
                </Switch>
            </HashRouter>
        );
    }
}

export default Main;