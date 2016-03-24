var Inliner = window.__APP__.Inliner;
var EipAllocate = window.__APP__.EipAllocate;

var UserRow = React.createClass({
  getInitialState() {
    return {
      showPool:false,
      deleting:null
    }
  },
  updateUser(user) {
    $.ajax({
      url: "/api/admin/users/" + user.id,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      type:'PUT',
      cache: false,
      contentType: 'application/json',
      data: JSON.stringify(user),
      success: function(data) {
        //noop
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  toggleInfo() {
    this.setState({showPool: !this.state.showPool});
  },
  delEip(eip) {
    if (!this.state.deleting) {
      this.setState({deleting:eip});
      $.ajax({
        url: "/api/admin/eips/" + eip.id,
        headers: { 'X-AUTH-TOKEN':Auth.getToken() },
        type: "DELETE",
        dataType: 'json',
        cache: false,
        success: function(data) {
          var shortList = this.props.user.eips.filter(function(anEip){
            return anEip.publicIp != eip.publicIp;
          })
          this.props.user.eips = shortList;
          this.setState({deleting:null});
        }.bind(this),
        error: function(xhr, status, err) {
          this.setState({deleting:null});
          console.error(this.props.url, status, err.toString());
        }.bind(this)
      });
    }
  },
  openAllocate(){
    this.refs.alloc.open();
  },
  addEip(data) {
    this.props.user.eips.push(data);
    this.refs.alloc.close();
    this.forceUpdate();
  },
  render() {
    return (
      <div className="row p-x-1 m-b-1">
        <div className="col-md-3 col-xs-12">
          <Inliner object={this.props.user} field="firstName"
            className="" handleEdit={this.updateUser} />
        </div>
        <div className="col-md-3 col-xs-12">
          <Inliner object={this.props.user} field="lastName"
            className="" handleEdit={this.updateUser} />
        </div>          
        <div className="col-md-3 col-xs-12">
          <Inliner object={this.props.user} field="email"
            className="" handleEdit={this.updateUser} />
        </div>      
        <div className="col-md-3 col-xs-12">
          <button className="btn btn-info" onClick={this.toggleInfo}>
            <i className="fa fa-info-circle"></i>
          </button>
          {this.state.showPool ?
            <div className="card" 
              style={{position:"absolute", top:"-1rem",right:"100%",width:"200%",zIndex:"99"}}>
              <div className="card-header card-info">
                <i className="fa btn btn-sm btn-info m-r-1 fa-times pull-right"
                      onClick={this.toggleInfo}></i>
                <i className="fa btn btn-sm btn-secondary m-r-1 fa-plus"
                      onClick={this.openAllocate}></i>
                EIP Pool
              </div>
              <div className='card-block'>
                {this.props.user.eips.map(function(eip) {
                  return (
                    <div key={eip.id}> 
                      <i className="fa btn btn-sm btn-danger m-r-1 fa-times"
                        onClick={this.delEip.bind(this, eip)}></i>
                      <strong>{eip.publicIp}: </strong> 
                      {eip.region} ({eip.domain})&emsp;
                      {eip.instanceId ? eip.instanceId : "not assigned"}

                    </div>
                  )
                }.bind(this))}
              </div>
            </div>
            :""
          }
          <EipAllocate ref="alloc"
            awsConfig={this.props.awsConfig}
            user={this.props.user}
            updateParent={this.addEip} />
        </div>      
      </div>
    );
  }
});

window.__APP__.UserPanel = React.createClass({
  getInitialState() {
    return {
      data:[],
      loading:false
    }
  },
  componentDidMount() {
    this.loadDataFromServer();
  },
  loadDataFromServer() {
    this.setState({loading:true});
    $.ajax({
      url: "/api/admin/users",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      success: function(data) {
       this.setState({data: data, loading:false});
      }.bind(this),
      error: function(xhr, status, err) {
         this.setState({loading:false});
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  addUser() {
    $.ajax({
      url: "/api/admin/users",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      type:'POST',
      cache: false,
      contentType: 'application/json',
      data: JSON.stringify({firstName:"New", lastName:"User"}),
      success: function(data) {
        this.loadDataFromServer();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render() {
    return (
      <div>
        {this.state.loading ?
          <div>
            <i className="fa fa-lg fa-spin fa-spinner"></i>
            Retrieving data from server...
          </div>
          :
          <div>
            <div className="m-b-1">
              <button className="btn btn-sm btn-success"
                  onClick={this.addUser}>
                Add User
              </button>
            </div>
              {this.state.data.map(function(user){
                return <UserRow user={user} awsConfig={this.props.awsConfig}
                    key={user.id}/>
              }.bind(this))}
          </div>
        }
      </div>
    );
  }
});