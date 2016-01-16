var Inliner = window.__APP__.Inliner;

var UserRow = React.createClass({
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
  render() {
    return (
      <div className="row p-x-1 m-b-1">
        <Inliner object={this.props.user} field="firstName"
          className="col-md-3 col-xs-12" handleEdit={this.updateUser} />
        <Inliner object={this.props.user} field="lastName"
          className="col-md-3 col-xs-12" handleEdit={this.updateUser} />
        <Inliner object={this.props.user} field="email"
          className="col-md-3 col-xs-12" handleEdit={this.updateUser} />
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
    console.debug(this.state.data);
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
                return <UserRow user={user} key={user.id}/>
              })}
          </div>
        }
      </div>
    );
  }
});