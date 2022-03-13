import React from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import CircularProgress from '@material-ui/core/CircularProgress';
import Alert from '@material-ui/lab/Alert';
import axios from 'axios';
import Center from 'react-center';

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
    avatar: {
        margin: theme.spacing(1),
        backgroundColor: theme.palette.secondary.main,
    },
    form: {
        width: '100%', // Fix IE 11 issue.
        marginTop: theme.spacing(1),
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    loader: {
        padding: '200px 0',
    }
});

class Login extends React.Component {
    constructor(props) {
        super(props);

        // This binding is necessary to make `this` work in the callback
        this.submitLogin = this.submitLogin.bind(this);
    }

    state = {
        inProgress: false,
        loginFailed: false,
        login: ''
    }

    handleChange = name => event => {
        this.setState({
            [name]: event.target.value,
        });
    };

    gotoChooseGame() {
      window.location.hash = "chooseGame";
    }

    submitLogin(event) {
        if (event != null) {
            event.preventDefault();
        }
        this.setState({ inProgress: true });
        const config = { headers: { 'Content-Type': 'application/json',
                                    'X-Requested-With': 'HttpRequest',
                                    'Csrf-Token': 'nocheck'},
                         timeout: 0};
        const data = new FormData();
        data.append('login', this.state['login']);
        axios.post("/login", data, config)
            .then( (response) => {
                if (response.status === 200) {
                    this.setState({ loginFailed: false });
                    this.gotoChooseGame();
                } else {
                    this.setState({ loginFailed: true });
                }
                this.setState({ inProgress: false });
            })
            .catch( (error) => {
                this.setState({ inProgress: false });
                this.setState({ loginFailed: true });
            });
    };

    render() {
        const { classes } = this.props;

        return (
            <Center className={classes.topMargin}>
              {!this.state.inProgress
               ?
               <Card className={classes.card}>
                 <CardContent>
                   <Container component="main" maxWidth="xs">
                     <CssBaseline />
                     <div className={classes.paper}>
                       <Avatar className={classes.avatar}>
                         <LockOutlinedIcon />
                       </Avatar>
                       <Typography component="h1" variant="h5">
                         Authentication
                       </Typography>
                       <form className={classes.form} noValidate>
                         {this.state.loginFailed
                          ? <Alert severity="error">
                              Authentication error. User already registered or login is empty.
                            </Alert>
                          : ""}
                         <TextField
                           variant="outlined"
                           margin="normal"
                           required
                           fullWidth
                           id="login"
                           label="Login"
                           name="login"
                           value={this.state.login}
                           onChange={this.handleChange('login')}
                           autoComplete="email" />
                         <Button
                           type="submit"
                           fullWidth
                           variant="contained"
                           color="primary"
                           className={classes.submit}
                           onClick={this.submitLogin}>
                           Login
                         </Button>
                       </form>
                     </div>
                   </Container>
                 </CardContent>                 
               </Card>
               : <div className={classes.loader}>
                   <CircularProgress className={classes.progress} size={90} />
                 </div>}
            </Center>
        );
    }
}

export default withStyles(styles)(Login);
