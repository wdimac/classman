var BootstrapModal = window.__APP__.BootstrapModal;
var Accordian = window.__APP__.Accordian;
var InlineSelect = window.__APP__.InlineSelect;

var SGLookup = React.createClass({
  getInitialState() {
    return {
      searchState:"waiting",
      regionGroups: [],
      currentModalRegion:""
    }
  },
  open() {
    this.refs.modal.open();
  },
  close() {
    this.refs.modal.close();
  },
  searchGroups() {
    // redo search if we have switched regions
    if (this.refs.region.value !== this.state.currentModalRegion) {
      this.setState({searchState:"searching"});
      $.ajax({
        url: "/api/admin/aws/" + this.refs.region.value + "/security_groups",
        headers: {'X-AUTH-TOKEN':Auth.getToken()},
        dataType: 'json',
        cache: false,
        success: function(data) {
          this.setState({
            regionGroups: data, 
            searchState:"waiting"
          });
        }.bind(this),
        error: function(xhr, status, err) {
          console.error(this.props.url, status, err.toString());
        }.bind(this)
      });
    }
  },
  addGroup(group) {
    $.ajax({
      url: "/api/admin/security_groups",
      type: "POST",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      contentType: 'application/json',
      dataType: 'json',
      data:JSON.stringify({
        id: group.groupId, 
        description: group.description, 
        region: this.refs.region.value,
        ownerId: group.ownerId,
        name: group.groupName,
        vpcId: group.vpcId
      }),
      cache: false,
      success: function(data) {
        this.props.updateParent();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render() {
    // build list of regions from AWS
    var foundGroups = null;
    if(this.state.searchState === 'searching') {
      foundGroups = (
        <div>
          <i className="fa fa-spinner fa-spin fa-2x fa-pull-left"></i>
          Loading data from AWS
        </div>
        );
    } else {
      var currentIds = this.props.existing.map(function(item){return item.id});
      foundGroups = this.state.regionGroups.filter(function(group){
          return currentIds.indexOf(group.groupId)<0;
        }.bind(this)).map(function(group){
          var boundClick = function(group){
            this.addGroup(group);
          }.bind(this, group);
          return(
            <div className="truncate" style={{padding:"2px"}} 
                onClick={boundClick} key={group.id}>
              <i className='fa fa-download btn btn-info btn-sm' /> &emsp;
              {group.groupId}: {group.groupName}
              {group.vpcId ? 
                <span> (VPC: {group.vpcId})</span>
                :""
              }
            </div>
          );
        }.bind(this));
    };
    // Build the select
    var regionSelect = this.props.awsConfig.regions ?
      (
        <select ref="region" placeholder="Select one">
          {this.props.awsConfig.regions.map(function(region) {
            return(
              <option value={region[0]} key={region[0]}>{region[1]}</option>
            );
          })}
        </select>
      )
      :"";

    //FInal composition
    return (
      <BootstrapModal
        ref="modal"
        onCancel={this.close}
        title="Lookup Security Groups">
        <div className="m-b-1">
          {regionSelect} &emsp;
          <button className="btn btn-sm btn-primary" onClick={this.searchGroups}>
            Search
          </button>
        </div>
        <div>
          {foundGroups}
        </div>
      </BootstrapModal>
    );
  }
});

window.__APP__.SecurityGroupPanel = React.createClass({
  getInitialState() {
    return {
      data:[],
      instructors:[]
    }
  },
  componentDidMount() {
    this.loadOther();
    this.loadDataFromServer();
  },
  loadOther() {
    $.ajax({
      url: "/api/admin/users",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      dataType: 'json',
      cache: false,
      success: function(data) {
        var options = [{name:"Select", value:""}];
        options = options.concat(data.map(function(instr){
          return {name: instr.firstName + " " + instr.lastName, value:instr.id}
        }));
        this.setState({instructors: options});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });  
  },
  loadDataFromServer() {
    $.ajax({
      url: "/api/admin/security_groups",
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
  openLookup() {
    this.refs.lookup.open();
  },
  updateGroup(secGroup) {
    $.ajax({
      url: "/api/admin/security_groups/" + secGroup.id,
      headers: { 'X-AUTH-TOKEN':Auth.getToken() },
      type: 'PUT',
      dataType: 'json',
      contentType: 'application/json',
      cache: false,
      data: JSON.stringify(secGroup),
      success: function(data) {
        console.log(data)
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(status, err.toString());
      }.bind(this)
    });
  },
  deleteGroup(secGroup) {
    if (!confirm("Delete this Security Group?")) {
        return;
    }
    $.ajax({
      url: "/api/admin/security_groups/" + secGroup.id,
      headers: { 'X-AUTH-TOKEN':Auth.getToken() },
      type: 'DELETE',
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.loadDataFromServer();
      }.bind(this),
        error: function(xhr, status, err) {
        console.error(status, err.toString());
      }.bind(this)
    });
  },
  //Callback for Accordian
  formatGroupRow(item) {
    var groupClass= item.defunct? "strike text-danger":"";
    return(
      <div key={item.id} className="truncate m-b-1">
        <div className="col-md-6">
        <i className="fa fa-times btn btn-danger"
            onClick={this.deleteGroup.bind(this, item)}></i>&emsp;
        <strong className={groupClass}>{item.id}</strong>:&emsp; 
        {item.vpcId ? 
          <span className="text-info">
            {item.vpcId}&ensp;
          </span>
          :""
        }
        {item.name} - {item.description} 
        
        </div>
        <div className="col-md-6">
          <InlineSelect object={item} field="instructor" isObject="true"
            options={this.state.instructors}
            className="" handleEdit={this.updateGroup} />
        </div>
      </div>
    )
  },
  render() {
    var sorted = window.__APP__.sortByRegion(this.state.data);
    return(
      <div>
        <div className="m-b-1">
          <button className="btn btn-sm btn-primary" onClick={this.openLookup}>
            Lookup Security Groups
          </button>
        </div>
        {this.state.data.length == 0 ?
          <i className="fa fa-spinner fa-spin fa-2x"></i>
          :
          <div className="row">
            <div className="m-l-1">
            <Accordian id="region_list" 
              formatItemRow={this.formatGroupRow}
              map={sorted} />
            </div>
          </div>
        }
        <SGLookup ref="lookup" 
          existing={this.state.data}
          awsConfig={this.props.awsConfig}
          updateParent={this.loadDataFromServer} />
      </div>
    );
  }
});