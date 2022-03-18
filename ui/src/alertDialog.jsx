import React from 'react';
import Button from '@material-ui/core/Button';
import {
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from '@material-ui/core';
import Alert from '@material-ui/lab/Alert';

class AlertDialog extends React.Component {
    handleClose = () => {
        this.props.parent.setState({
            [this.props.propertyName]: false
        });
        if (this.props.handler != null) {
            this.props.handler();
        }
    };

    render() {
        const { ...other } = this.props;

        return (
            <div>
              <Dialog
                onClose={this.handleClose}
                { ...other }>
                <DialogTitle id="alert-dialog-title">{this.props.title}</DialogTitle>
                <DialogContent>
                  <DialogContentText id="alert-dialog-description">
                    {this.props.severity == null
                     ? this.props.message
                     : <Alert severity={this.props.severity}>
                         {this.props.message}
                       </Alert>}
                  </DialogContentText>
                </DialogContent>
                <DialogActions>
                  <Button onClick={this.handleClose} color="primary" autoFocus>
                    Ok
                  </Button>
                </DialogActions>
              </Dialog>
            </div>
        );
    }
}

export default AlertDialog;
