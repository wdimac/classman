/**
 * Copyright (C) 2016 AppDynamics
 */
// Login Panel component
window.__APP__.LoginPanel = React.createClass({
	getInitialState() {
		return {
			msg:null
		}
	},
	handleSubmit(event) {
		event.preventDefault();
		var uname = ReactDOM.findDOMNode(this.refs.username).value.trim();
		var pword = ReactDOM.findDOMNode(this.refs.password).value.trim();
		this.setState({msg:null});
		Auth.logIn(uname, pword, function(authenticated) {
			if (!authenticated) {
				this.setState({msg:"Login failed."});
			}
			this.props.callback(authenticated)
		}.bind(this));
	},
	render() {
		return(
			<div className='card center-block' style={{width:"350px"}}>
				<div className='card-header bg-primary'>
					Login
				</div>
				<div className="card-body p-a-1">
					<form onSubmit={this.handleSubmit}>
						<div className='form-group'>
							<input id='username' ref='username' 
								className='form-control'
								autoCapitalize='none'
								placeholder='Username' type='text'/>
						</div>
						<div className='form-group'>
							<input id='password' ref='password' 
								className='form-control'
								placeholder='Password' type='password'/>
						</div>
						
						<div className='form-group'>
							<input id='login_submit' type='submit' value='>>' 
									className='btn btn-small btn-info'/>
							<span>{this.state.msg}</span>
						</div>
					</form>
				</div>
			</div>
		);
	}
});
