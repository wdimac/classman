/**
 * Copyright (C) 2016 AppDynamics
 */
var LoginPanel = window.__APP__.LoginPanel;
var Admin = window.__APP__.Admin;

// Main Application component
var App = React.createClass({
	getInitialState() {
		return {
			loggedIn: Auth.loggedIn()
		}
	},
	setAuth(authenticated) {
		this.setState({loggedIn: authenticated});
	},
	componentWillMount() {
		Auth.onChange = this.setAuth;
	},
	render() {
		if (this.state.loggedIn) {
			return(
				<div>
					<NavBar />
					<MainPanel />
					<FooterBar />
				</div>
			);
		} else {
			return(<LoginPanel callback={this.setAuth}/>);
		}
	}
});

// Main display panel
var MainPanel = React.createClass({
	render() {
		return(
			<div className="card p-a-1" >
				<Admin />
			</div>
		);
	}
});

// Grab navigation componenets from nav.jsx
var NavBar = window.__APP__.NavBar;
var FooterBar = window.__APP__.FooterBar;

// Render initial view
ReactDOM.render(
  <App/>,
  document.getElementById('react_container')
);
