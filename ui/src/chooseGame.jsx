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
    }

    state = {
        singleCardGameSelected: false
    }

    askSingleCardGame(_) {
        this.setState({ singleCardGameSelected: true});
        const config = { headers: { 'Content-Type': 'application/json',
                                    'X-Requested-With': 'HttpRequest',
                                    'Csrf-Token': 'nocheck'},
                         timeout: 0};
        const data = new FormData();
        data.append('gameType', "singleCardGame");
        axios.post("/askForGame", data, config)
            .then((response) => {
                if (response.status === 200) {
                    window.location.hash = "singleCardGame";
                } else if (response.status === 204) {
                    setTimeout(this.askSingleCardGame, 2000);
                }
            })
            .catch( (error) => {
                console.log(error);
            });        
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
                           onClick={this.doubleCardGame}>
                           Double card game
                         </Button>
                        </Container>
                    </CardContent>
                </Card>
            </Center>)
    }
}

export default withStyles(styles)(ChooseGame);