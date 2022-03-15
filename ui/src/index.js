import React from 'react';
import { render } from 'react-dom';
import Main from './main';
import AppContext from './appContext';

const rootElement = document.querySelector('#root');
if (rootElement) {
    render(
        <AppContext.Provider value={{ login: '', gameId: ''}}>
            <Main/>
        </AppContext.Provider>,
        rootElement);
}
