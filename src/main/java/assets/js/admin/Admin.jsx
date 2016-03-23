var Images = window.__APP__.Images;
var Instances = window.__APP__.Instances;
var SecurityGroupPanel = window.__APP__.SecurityGroupPanel;
var EipPanel = window.__APP__.EipPanel;
var ClassTypePanel = window.__APP__.ClassTypePanel;
var ClassesPanel = window.__APP__.ClassesPanel;
var UserPanel = window.__APP__.UserPanel;
var VpcPanel = window.__APP__.VpcPanel;

var MenuItem = React.createClass({
  render() {
    var cname = "nav-item nav-link";
    if (this.props.isActive)
      cname += " active";
    return(
      <a href="javacript:void(0);" className={cname}
                onClick={this.props.click}>
              <i className={"fa fa-" + this.props.icon}> </i>
              <span className="hidden-xs-down"> {this.props.title}</span>
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
			active: 'classes'
		}
	},
  componentDidMount() {
    this.loadDataFromServer();
  },
  changePanel(panel, event) {
    event.preventDefault();
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
      case 'groups':
        panel = (<SecurityGroupPanel awsConfig={this.state.awsConfig} />);
        break;
      case 'eips':
        panel = (<EipPanel awsConfig={this.state.awsConfig} />);
        break;
      case 'class_type':
        panel = (<ClassTypePanel awsConfig={this.state.awsConfig} />);
        break;
      case 'classes':
        panel = (<ClassesPanel awsConfig={this.state.awsConfig} />);
        break;
      case 'vpc':
        panel = (<VpcPanel awsConfig={this.state.awsConfig} />);
        break;
      case 'users':
        panel = (<UserPanel awsConfig={this.state.awsConfig} />);
        break;
      default:
        panel = (<div> Under construction </div>)
		}
		return (
			<div className="card">
				<div className="navbar navbar-light">
					<span className='navbar-brand'>Manage: </span>
					<span className='nav navbar-nav'>
            <MenuItem isActive={this.state.active === 'classes'}
              click={this.changePanel.bind(this, 'classes')}
              icon='calendar'
              title="Classes" />
            <MenuItem isActive={this.state.active === 'class_type'}
              click={this.changePanel.bind(this, 'class_type')}
              icon='calendar-o'
              title="Class Types" />
            <MenuItem isActive={this.state.active === 'instances'}
              click={this.changePanel.bind(this, 'instances')}
              icon='desktop'
              title="All Instances" />
            <MenuItem isActive={this.state.active === 'images'}
              click={this.changePanel.bind(this, 'images')}
              icon='image'
              title="All Images" />
            <MenuItem isActive={this.state.active === 'eips'}
              click={this.changePanel.bind(this, 'eips')}
              icon='map-marker'
              title="EIPs" />
            <MenuItem isActive={this.state.active === 'groups'}
              click={this.changePanel.bind(this, 'groups')}
              icon='key'
              title="Sec. Groups" />
            <MenuItem isActive={this.state.active === 'vpc'}
              click={this.changePanel.bind(this, 'vpc')}
              icon='sitemap'
              title="VPC" />
            <MenuItem isActive={this.state.active === 'users'}
              click={this.changePanel.bind(this, 'users')}
              icon='users'
              title="Users" />
					</span>
				</div>
				<div className="card-body p-a-1">
					{panel}
				</div>
			</div>
			);
	}
});

