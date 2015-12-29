var Images = window.__APP__.Images;

window.__APP__.Admin = React.createClass({
  loadDataFromServer() {
    $.ajax({
      url: "/api/admin/config",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      success: function(config) {
        this.setState({awsConfig: config});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
	getInitialState() {
		return {
			awsConfig: {},
			active: 'images'
		}
	},
  componentDidMount() {
    this.loadDataFromServer();
  },	
  render() {
		var panel = "";
		switch(this.state.active) {
			case 'images':
				panel = (<Images awsConfig={this.state.awsConfig}/>);
		}

		return (
			<div className="card">
				<div className="navbar navbar-light">
					<span className='navbar-brand'>Manage: </span>
					<span className='nav navbar-nav'>
						<a href="javacript:void(0);" className="nav-item nav-link active">
							All Images
						</a>
						<a href="javacript:void(0);" className="nav-item nav-link">
							All Instances
						</a>
						<a href="javacript:void(0);" className="nav-item nav-link">
							EIPs
						</a>
					</span>
				</div>
				<div className="card-body" style={{padding:"1rem"}}>
					{panel}
				</div>
			</div>
			);
	}
});

