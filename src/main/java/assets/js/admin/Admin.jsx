var Images = window.__APP__.Images;
var Instances = window.__APP__.Instances;

var MenuItem = React.createClass({
  render() {
    var cname = "nav-item nav-link";
    if (this.props.isActive)
      cname += " active";
    return(
      <a href="javacript:void(0);" className={cname}
                onClick={this.props.click}>
              {this.props.title}
            </a>
    ); 
  }
});

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
			active: 'instances'
		}
	},
  componentDidMount() {
    this.loadDataFromServer();
  },	
  changePanel(panel) {
    this.setState({active:panel});
  },
  render() {
		var panel = "";
		switch(this.state.active) {
			case 'images':
        panel = (<Images awsConfig={this.state.awsConfig}/>);
        break;
      case 'instances':
        panel = (<Instances awsConfig={this.state.awsConfig} />);
        break;
		}
		return (
			<div className="card">
				<div className="navbar navbar-light">
					<span className='navbar-brand'>Manage: </span>
					<span className='nav navbar-nav'>
            <MenuItem isActive={this.state.active === 'instances'}
              click={this.changePanel.bind(this, 'instances')}
              title="All Instances" />
            <MenuItem isActive={this.state.active === 'images'}
              click={this.changePanel.bind(this, 'images')}
              title="All Images" />
            <MenuItem isActive={this.state.active === 'eips'}
              click={this.changePanel.bind(this, 'eips')}
              title="EIPs" />
					</span>
				</div>
				<div className="card-body p-a-1">
					{panel}
				</div>
			</div>
			);
	}
});

