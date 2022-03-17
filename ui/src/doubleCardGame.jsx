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

class DoubleCardGame extends React.Component {
    constructor(props) {
        super(props);

        // This binding is necessary to make `this` work in the callback
        this.play = this.play.bind(this);
        this.fold = this.fold.bind(this);
        this.submitAction = this.submitAction.bind(this);
        this.checkState = this.checkState.bind(this);
    }    
    blank = [{rank: 0, suit: '', imagePath: "../images/blank.png"},
             {rank: 0, suit: '', imagePath: "../images/blank.png"}];

    state = {
        login: '',
        tokens: 0,
        turnIndex: 0,
        cards: this.blank,
        action: ''
    }

    getCardImage = (card) => {
        return <img width='150px' src={card.imagePath}></img>
    }

    play(_) {
        this.submitAction("Play")
    }

    fold(_) {
        this.submitAction("Fold")
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
                                    cards: (response.data.cards.length === 2)
                                        ? response.data.cards
                                        : this.blank,
                                    action: response.data.action,
                                    tokens: response.data.tokens,
                                    turnIndex: response.data.turnIndex});
                    if (response.data.action != '') {
                        setTimeout(this.checkState(false), 2000);
                    }                    
                }
            })
            .catch( (error) => {
                console.log(error);
            });                
    }

    checkState = (init) => (event) => {
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
                                    cards: (response.data.cards.length === 2)
                                        ? response.data.cards
                                        : this.blank,
                                    action: response.data.action.name,
                                    tokens: response.data.tokens,
                                    turnIndex: response.data.turnIndex});
                    if (!init && response.data.action != '') {
                        setTimeout(this.checkState(false), 2000);
                    }                    
                } else {
                    console.log(response);
                }
            })
            .catch( (error) => {
                console.log(error);
            });
    };

    componentDidMount() {
        this.checkState(true)(null);
    }

    render() {
        const { classes } = this.props;        

        return (
            <Center className={classes.topMargin}>
                <Card className={classes.card}>
                    <CardContent>                        
                        <Container component="main" maxWidth="xs">
                        <Center>
                            <Typography id="1" variant="h5" component="h2">
                                Double Card Game
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
                                            {this.getCardImage(this.state.cards[0])}
                                            {this.getCardImage(this.state.cards[1])}
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
                        </table>
                        </Container>
                    </CardContent>
                </Card>
            </Center>)
    }
}

export default withStyles(styles)(DoubleCardGame);