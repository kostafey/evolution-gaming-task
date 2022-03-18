import React from 'react';

import { withStyles } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Center from 'react-center';
import Container from '@material-ui/core/Container';
import Typography from '@material-ui/core/Typography';
import axios from 'axios';
import AlertDialog from './alertDialog';

const styles = theme => ({
    paper: {
        marginTop: theme.spacing(8),
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    topMargin: {
      position: 'relative', top: '100px'
    },
    form: {
        width: '100%', // Fix IE 11 issue.
        marginTop: theme.spacing(1),
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    table: {
        width: '100%'
    }
});

class SingleCardGame extends React.Component {
    constructor(props) {
        super(props);

        // This binding is necessary to make `this` work in the callback
        this.play = this.play.bind(this);
        this.fold = this.fold.bind(this);
        this.finish = this.finish.bind(this);
        this.submitAction = this.submitAction.bind(this);
        this.checkState = this.checkState.bind(this);
        this.getSummary = this.getSummary.bind(this);
    }

    state = {
        login: '',
        tokens: 0,
        turnIndex: 0,
        card: {rank: 0, suit: '', imagePath: "../images/blank.png"},
        action: '',
        isLast: false,
        showFinishGameDialog: false,
        summary: ''
    }

    getCardImage = (card) => {
        return <img width='300px' src={card.imagePath}></img>
    }

    play(_) {
        this.submitAction("Play")
    }

    fold(_) {
        this.submitAction("Fold")
    }

    finish(_) {
        this.submitAction("Finish")
    }

    gotoChooseGame() {
        window.location.hash = "chooseGame";
    }

    submitAction(action) {
        this.setState({ action: action });
        const config = { headers: { 'Content-Type': 'application/json',
                                    'X-Requested-With': 'HttpRequest',
                                    'Csrf-Token': 'nocheck'},
                         timeout: 0};
        const data = new FormData();
        data.append('login', this.props.context.login);
        data.append('gameId', this.props.context.gameId);
        data.append('action', action);
        data.append('turnIndex', this.state.turnIndex);
        axios.post("/submitAction", data, config)
            .then((response) => {
                if (response.status === 200) {
                    this.setState({ login: response.data.login,
                                    card: (response.data.cards.length > 0)
                                        ? response.data.cards[0]
                                        : {rank: 0, suit: '', imagePath: "../images/blank.png"},
                                    action: response.data.action,
                                    tokens: response.data.tokens,
                                    turnIndex: response.data.turnIndex,
                                    isLast: response.data.isLast});
                    if (response.data.action != '') {
                        setTimeout(this.checkState, 2000);
                    }
                }
            })
            .catch( (error) => {
                console.log(error);
            });
    }

    checkState(event) {
        if (event != null) {
            event.preventDefault();
        }
        const config = { headers: { 'Content-Type': 'application/json',
                                    'X-Requested-With': 'HttpRequest',
                                    'Csrf-Token': 'nocheck'},
                         timeout: 0};
        const data = new FormData();
        data.append('login', this.props.context.login);
        data.append('gameId', this.props.context.gameId);
        axios.post("/getGameState", data, config)
            .then( (response) => {
                if (response.status === 200) {
                    this.setState({ login: response.data.login,
                                    card: (response.data.cards.length > 0)
                                        ? response.data.cards[0]
                                        : {rank: 0, suit: '', imagePath: "../images/blank.png"},
                                    action: response.data.action.name,
                                    tokens: response.data.tokens,
                                    turnIndex: response.data.turnIndex,
                                    isLast: response.data.isLast});
                    if (response.data.action != '' && !response.data.isLast) {
                        setTimeout(this.checkState, 2000);
                    }
                    if (response.data.isLast) {
                        this.getSummary()
                        this.setState({showFinishGameDialog: true });
                    }
                } else {
                    console.log(response);
                }
            })
            .catch( (error) => {
                console.log(error);
            });
    };

    getSummary() {
        const config = { headers: { 'Content-Type': 'application/json',
                                    'X-Requested-With': 'HttpRequest',
                                    'Csrf-Token': 'nocheck'},
                         timeout: 0};
        const data = new FormData();
        data.append('login', this.props.context.login);
        data.append('gameId', this.props.context.gameId);
        axios.post("/getSummary", data, config)
            .then( (response) => {
                if (response.status === 200) {
                    this.setState({ summary: JSON.stringify(response.data)});
                } else {
                    console.log(response);
                }
            })
            .catch( (error) => {
                console.log(error);
            });
    };

    componentDidMount() {
        this.checkState(null);
    }

    render() {
        const { classes } = this.props;

        return (
            (this.state.isLast && this.state.showFinishGameDialog)
            ? <AlertDialog
                     open={true}
                     propertyName='showFinishGameDialog'
                     handler={this.gotoChooseGame}
                     parent={this}
                     title="Game finished"
                     message={this.state.summary}/>
            : <Center className={classes.topMargin}>
                <Card className={classes.card}>
                    <CardContent>
                        <Container component="main" maxWidth="xs">
                        <Center>
                            <Typography id="1" variant="h5" component="h2">
                                Single Card Game
                            </Typography>
                        </Center>
                        <table className={classes.table}>
                            <tr>
                                <td colspan="2" align='right'>
                                    Player: {this.props.context.login}
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align='right'>
                                    Tokens: {this.state.tokens}
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align='right'>
                                    Turn: {this.state.turnIndex}
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <Center>
                                        <Box component="span" m={2}>
                                            {this.getCardImage(this.state.card)}
                                        </Box>
                                    </Center>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <Button
                                        type="submit"
                                        fullWidth
                                        variant="contained"
                                        color="secondary"
                                        className={classes.submit}
                                        onClick={this.play}
                                        disabled={this.state.action != ''}>
                                        Play
                                    </Button>
                                </td>
                                <td>
                                    <Button
                                        type="submit"
                                        fullWidth
                                        variant="contained"
                                        color="primary"
                                        className={classes.submit}
                                        onClick={this.fold}
                                        disabled={this.state.action != ''}>
                                        Fold
                                    </Button>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <Button
                                        type="submit"
                                        fullWidth
                                        variant="contained"
                                        className={classes.submit}
                                        onClick={this.finish}
                                        disabled={this.state.action != ''}>
                                        Finish
                                    </Button>
                                </td>
                            </tr>
                        </table>
                        </Container>
                    </CardContent>
                </Card>
            </Center>)
    }
}

export default withStyles(styles)(SingleCardGame);