/**
 * Copyright (C) 2016 AppDynamics
 */

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
// Top navigation bar
var NavBar = React.createClass({
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
var FooterBar = React.createClass({
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

// Login Panel component
var LoginPanel = React.createClass({
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
				<div className="card-body" style={{padding:"10px"}}>
										<form onSubmit={this.handleSubmit}>
						<div className='form-group'>
							<input id='username' ref='username' 
								className='form-control'
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

// Render initial view
ReactDOM.render(
  <App/>,
  document.getElementById('react_container')
);
