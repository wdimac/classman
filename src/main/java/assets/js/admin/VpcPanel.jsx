var BootstrapModal = window.__APP__.BootstrapModal;
var Accordian = window.__APP__.Accordian;
var Select = window.__APP__.Select;

var VpcLookup = React.createClass({
  getInitialState() {
    return {
      searchState:"waiting",
      regionVpcs: [],
      currentModalRegion:""
    }
  },
  open() {
    this.setState({regionVpcs:[]});
    this.refs.modal.open();
  },
  close() {
    this.refs.modal.close();
  },
  searchVpcs() {
    // redo search if we have switched regions
    if (this.refs.region.getValue() !== this.state.currentModalRegion) {
      this.setState({searchState:"searching"});
      $.ajax({
        url: "/api/admin/aws/" + this.refs.region.getValue() + "/vpc",
        headers: {'X-AUTH-TOKEN':Auth.getToken()},
        dataType: 'json',
        cache: false,
        success: function(data) {
          this.setState({
            regionVpcs: data, 
            searchState:"waiting"
          });
        }.bind(this),
        error: function(xhr, status, err) {
          console.error(this.props.url, status, err.toString());
        }.bind(this)
      });
    }
  },
  addVpc(vpc) {
    $.ajax({
      url: "/api/admin/vpc",
      type: "POST",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      contentType: 'application/json',
      dataType: 'json',
      data:JSON.stringify({
        vpcId: vpc.vpcId, 
        subnetId: vpc.subnetId, 
        region: this.refs.region.getValue()
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
    var foundVpcs = null;
    if(this.state.searchState === 'searching') {
      foundVpcs = (
        <div>
          <i className="fa fa-spinner fa-spin fa-2x fa-pull-left"></i>
          Loading data from AWS
        </div>
        );
    } else {
      var currentIds = this.props.existing.map(function(item){return item.subnetId});
      foundVpcs = this.state.regionVpcs.filter(function(vpc){
          return currentIds.indexOf(vpc.subnetId  )<0;
        }.bind(this)).map(function(vpc){
          var boundClick = function(vpc){
            this.addVpc(vpc);
          }.bind(this, vpc);
          return(
            <div className="truncate" style={{padding:"2px"}} 
                onClick={boundClick} key={vpc.subnetId}>
              <i className='fa fa-download btn btn-info btn-sm' /> &emsp;
              {vpc.vpcId}:{vpc.subnetId}
            </div>
          );
        }.bind(this));
      if (foundVpcs.length == 0) {
        foundVpcs = (<div>No items found</div>);
      }
    };
    // Build the select
    var regionOptions = this.props.awsConfig.regions ?
      this.props.awsConfig.regions.map(function(region) {
        return {value:region[0], name:region[1]};
      }):[];

    //FInal composition
    return (
      <BootstrapModal
        ref="modal"
        onCancel={this.close}
        title="Lookup VPCs">
        <div className="m-b-1">
          <Select ref="region" options={regionOptions} />
          <button className="btn btn-sm btn-primary" onClick={this.searchVpcs}>
            Search
          </button>
        </div>
        <div>
          {foundVpcs}
        </div>
      </BootstrapModal>
    );
  }
});


window.__APP__.VpcPanel = React.createClass({
  getInitialState() {
    return {
      data:[],
      isSearching: false,
      deleting:null
    }
  },
  componentDidMount() {
    this.loadDataFromServer();
  },
  loadDataFromServer() {
    this.setState({isSearching: true});
    $.ajax({
      url: "/api/admin/vpc",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data, isSearching:false});
      }.bind(this),
      error: function(xhr, status, err) {
        this.setState({isSearching:false})
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  openLookup() {
    this.refs.lookup.open();
  },
  del(item) {
    if (!this.state.deleting) {
      this.setState({deleting:item});
      $.ajax({
        url: "/api/admin/vpc/" + item.subnetId,
        headers: { 'X-AUTH-TOKEN':Auth.getToken() },
        type: "DELETE",
        dataType: 'json',
        cache: false,
        success: function(data) {
          this.setState({deleting:null});
          this.loadDataFromServer();
        }.bind(this),
        error: function(xhr, status, err) {
          this.setState({deleting:null});
          console.error(this.props.url, status, err.toString());
        }.bind(this)
      });
    }
  },
  //Callback for Accordian
  formatVpcRow(item) {
    var delThis = this.state.deleting && (item.subnetId == this.state.deleting.subnetId);
    return(
      <div key={item.vpcId} className="truncate">
        <i className={"fa btn btn-sm btn-danger m-r-1 " + (delThis ? "fa-hourglass-half" : "fa-times")}
          onClick={this.del.bind(this, item)}></i>
        <strong>{item.vpcId}:{item.subnetId} </strong> 
        {item.isDefault ? "(default)" : ""}
      </div>
    )
  },
  render() {
    var sorted = window.__APP__.sortByRegion(this.state.data);
    return(
      <div>
        <div className="m-b-1">
          <button className="btn btn-sm btn-primary" onClick={this.openLookup}>
            Search for VPCs
          </button>
        </div>
        {this.state.isSearching ?
          <div>
            <i className="fa fa-spinner fa-spin fa-2x"></i> retrieving data...
          </div>
          :
          <div className="row">
            <div className="m-l-1">

              <Accordian id="region_list" 
                formatItemRow={this.formatVpcRow}
                map={sorted} />
            </div>
          </div>
        }

        <VpcLookup ref="lookup" 
          existing={this.state.data}
          awsConfig={this.props.awsConfig}
          updateParent={this.loadDataFromServer} />
      </div>
    );
  }
});