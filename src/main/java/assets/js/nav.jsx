/**
 * Copyright (C) 2016 Appdynamics
 */

// Top navigation bar
window.__APP__.NavBar = React.createClass({
	doLogOut() {
		Auth.logOut();
	},
	render() {
		return(
			<div className="navbar navbar-full navbar-dark bg-inverse">
				<div className="navbar-brand">
					Classroom Manager
				</div>
				<button type="button" className="btn btn-small btn-secondary pull-right" aria-label="Close"
						onClick={this.doLogOut}>
				  <span aria-hidden="true">&times;</span>
				</button>
			</div>
		);
	}
});

// Footer
window.__APP__.FooterBar = React.createClass({
	render() {
		return(
			<div className="navbar navbar-full navbar-dark bg-inverse">
				<small>
					Copyright &copy; 2016 AppDynamics, Inc.
				</small>
			</div>
		);
	}
});
