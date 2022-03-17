import React from 'react';
import { withStyles } from '@material-ui/core/styles';
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
    }
});

class ChooseGame extends React.Component {
    constructor(props) {
        super(props);
        // This binding is necessary to make `this` work in the callback
        this.askSingleCardGame = this.askSingleCardGame.bind(this);
        this.askDoubleCardGame = this.askDoubleCardGame.bind(this);
    }

    state = {
        singleCardGameSelected: false,
        doubleCardGameSelected: false
    }

    askSingleCardGame(_) {        
        this.setState({ singleCardGameSelected: true});
        this.askForGame("single-card-game", "singleCardGame");
    }

    askDoubleCardGame(_) {        
        this.setState({ doubleCardGameSelected: true});
        this.askForGame("double-card-game", "doubleCardGame");
    }

    askForGame(gameType, redirectTo) {
        {
            const config = { headers: { 'Content-Type': 'application/json',
                                        'X-Requested-With': 'HttpRequest',
                                        'Csrf-Token': 'nocheck'},
                             timeout: 0};
            const data = new FormData();
            data.append('login', this.props.context.login);
            data.append('gameType', gameType);
            axios.post("/askForGame", data, config)
                .then((response) => {
                    if (response.status === 200) {
                        this.props.context.gameId = response.data.gameId;
                        window.location.hash = redirectTo;
                    } else if (response.status === 204) {
                        setTimeout(() => 
                                this.askForGame(gameType, redirectTo), 
                            2000);
                    }
                })
                .catch( (error) => {
                    console.log(error);
                });        
        }
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
                                Choose Game type
                            </Typography>
                        </Center>
                        <Button
                           type="submit"
                           fullWidth
                           variant="contained"
                           color="primary"
                           className={classes.submit}
                           onClick={this.askSingleCardGame}
                           disabled={this.state.singleCardGameSelected}>
                           Single card game
                         </Button>
                         <Button
                           type="submit"
                           fullWidth
                           variant="contained"
                           color="primary"
                           className={classes.submit}                           
                           onClick={this.askDoubleCardGame}
                           disabled={this.state.doubleCardGameSelected}>
                           Double card game
                         </Button>
                        </Container>
                    </CardContent>
                </Card>
            </Center>)
    }
}

export default withStyles(styles)(ChooseGame);