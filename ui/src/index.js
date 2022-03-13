import React from 'react';
import { render } from 'react-dom';
import Main from './main';

const rootElement = document.querySelector('#root');
if (rootElement) {
    render(
        <Main/>,
        rootElement);
}
