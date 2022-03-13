import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import {Dialog, DialogActions, DialogContent, DialogTitle,
        DialogContentText} from '@material-ui/core';
import TextField from '@material-ui/core/TextField';

class PromptDialog extends React.Component {

    state = {};

    handleCancel = () => {
        this.props.onClose('cancel');
    };

    handleOk = () => {
        this.props.onClose('ok');
    };

    handleChange = name => event => {
        this.props.parent.setState({
            [name]: event.target.value,
        });
    };

    render() {
        const { ...other } = this.props;

        return (
            <Dialog {...other}>
              <DialogTitle id="prompt-dialog-title">
                {this.props.title}
              </DialogTitle>
              <DialogContent>
                <DialogContentText>
                  {this.props.message}
                </DialogContentText>
                <TextField
                  value={this.props.parent.state.email}
                  onChange={this.handleChange(this.props.stateName)}
                  autoFocus
                  margin="dense"
                  id="name"
                  label={this.props.label}
                  type={this.props.type}
                  fullWidth
                />
                {(this.props.stateName2 != null)
                ? <TextField
                    value={this.props.parent.state[this.props.stateName2]}
                    onChange={this.handleChange(this.props.stateName2)}
                    autoFocus
                    margin="dense"
                    id="name"
                    label={this.props.label2}
                    type={this.props.type2}
                    fullWidth
                  />
                : ""}
              </DialogContent>
              <DialogActions>
                <Button onClick={this.handleCancel} color="default">
                  Cancel
                </Button>
                <Button onClick={this.handleOk} color="primary" autoFocus>
                  Ok
                </Button>
              </DialogActions>
            </Dialog>
        );
    }
}

const styles = theme => ({
  root: {
    width: '100%',
  },
});

export default withStyles(styles)(PromptDialog);
