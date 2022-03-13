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

const cards = {
    14: "../images/ace-of-clubs.png",   // Ace
    13: "../images/king-of-clubs.png",  // King
    12: "../images/queen-of-clubs.png", // Queen
    11: "../images/jack-of-clubs.png",  // Jack
    10: "../images/ten-of-clubs.png",   // Ten
    9:  "../images/nine-of-clubs.png",  // Nine
    8:  "../images/eight-of-clubs.png", // Eight
    7:  "../images/seven-of-clubs.png", // Seven
    6:  "../images/six-of-clubs.png",   // Six
    5:  "../images/five-of-clubs.png",  // Five
    4:  "../images/four-of-clubs.png",  // Four
    3:  "../images/three-of-clubs.png", // Three
    2:  "../images/two-of-clubs.png"    // Two
}

class SingleCardGame extends React.Component {
    constructor(props) {
        super(props);

        // This binding is necessary to make `this` work in the callback
        this.checkState = this.checkState.bind(this);
    }    

    state = {
        login: '',
        tokens: 0,
        turn: 0,
        action: null,
        actionCompleted: false
    }

    getCardImage = (rank) => {
        return <img width='300px' src={cards[rank]}></img>
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
        ['login', 'turn', 'action'].forEach(f => {data[f] = this.state[f];})        
        axios.get("/getSingleCardGameState", data, config)
            .then( (response) => {
                if (response.status === 200) {                    
                    this.setState({ login: response.data.login,
                                    tokens: response.data.tokens,
                                    turn: response.data.turnIndex,
                                    actionCompleted: false});
                    
                } else {
                    console.log(response);
                }
            })
            .catch( (error) => {
                console.log(error);
            });
    };

    render() {
        const { classes } = this.props;
        // this.checkState();

        return (
            <Center className={classes.topMargin}>
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
                                    Player: {this.state.login}
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align='right'>
                                    Score: {this.state.tokens}
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align='right'>
                                    Turn: {this.state.turn}
                                </td>
                            </tr>                            
                            <tr>
                                <td colspan="2">
                                    <Center>
                                        <Box component="span" m={2}>
                                            {this.getCardImage(14)}
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
                                        onClick={this.checkState}>
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
                                        onClick={this.doubleCardGame}>
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

export default withStyles(styles)(SingleCardGame);