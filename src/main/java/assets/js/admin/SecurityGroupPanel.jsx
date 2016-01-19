var BootstrapModal = window.__APP__.BootstrapModal;
var Accordian = window.__APP__.Accordian;

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
          console.debug(group);
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
      data:[]
    }
  },
  componentDidMount() {
    this.loadDataFromServer();
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
  //Callback for Accordian
  formatGroupRow(item) {
    return(
      <div key={item.id} className="truncate">
        <strong>{item.id}:</strong> 
        <span className="text-muted"> (Owner: {item.ownerId}) </span>
        {item.name} - 
        {item.description} 
        {item.vpcId ? 
          <span className="text-info m-l-1">
            {item.vpcId}
          </span>
          :""
        }
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