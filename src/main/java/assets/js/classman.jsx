/**
 * Copyright (C) 2016 AppDynamics
 */

var LoginPanel = window.__APP__.LoginPanel;

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
	loadDataFromServer() {
    $.ajax({
      url: "/api/testdata",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
    	},
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },

	getInitialState() {
		return {
			data: []
		}
	},

	componentDidMount() {
    this.loadDataFromServer();
    setInterval(this.loadDataFromServer, 3000);
  },

	render() {
		var testlist = this.state.data.map(function(test) {
      return (
        <div key={test.id}>
          {test.title}
        </div>
      );
    });
		return(
			<div className="card" style={{padding:"20px"}}>
				Data found in the Test table (this is polled every 3 sec.):
				{testlist}
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
