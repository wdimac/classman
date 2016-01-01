var BootstrapModal = window.__APP__.BootstrapModal;
var Accordian = window.__APP__.Accordian;
var Select = window.__APP__.Select;

var EipLookup = React.createClass({
  getInitialState() {
    return {
      searchState:"waiting",
      regionEips: [],
      currentModalRegion:""
    }
  },
  open() {
    this.refs.modal.open();
  },
  close() {
    this.refs.modal.close();
  },
  searchEips() {
    // redo search if we have switched regions
    if (this.refs.region.value !== this.state.currentModalRegion) {
      this.setState({searchState:"searching"});
      $.ajax({
        url: "/api/admin/aws/" + this.refs.region.value + "/eips",
        headers: {'X-AUTH-TOKEN':Auth.getToken()},
        dataType: 'json',
        cache: false,
        success: function(data) {
          console.debug(data);
          this.setState({
            regionEips: data, 
            searchState:"waiting"
          });
        }.bind(this),
        error: function(xhr, status, err) {
          console.error(this.props.url, status, err.toString());
        }.bind(this)
      });
    }
  },
  addEip(eip) {
    $.ajax({
      url: "/api/admin/eips",
      type: "POST",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      contentType: 'application/json',
      dataType: 'json',
      data:JSON.stringify({
        publicIp: eip.publicIp, 
        description: eip.publicIp, 
        region: this.refs.region.value,
        instanceId: eip.instanceId,
        allocationId: eip.allocationId,
        associationId: eip.associationId,
        domain: eip.domain,
        networkInterfaceId: eip.networkInterfaceId,
        networkInterfaceOwnerId: eip.networkInterfaceOwnerId,
        privateIpAddress: eip.privateIpAddress
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
    var foundEips = null;
    if(this.state.searchState === 'searching') {
      foundEips = (
        <div>
          <i className="fa fa-spinner fa-spin fa-2x fa-pull-left"></i>
          Loading data from AWS
        </div>
        );
    } else {
      var currentIds = this.props.existing.map(function(item){return item.publicIp});
      foundEips = this.state.regionEips.filter(function(eip){
          return currentIds.indexOf(eip.publicIp  )<0;
        }.bind(this)).map(function(eip){
          var boundClick = function(eip){
            this.addEip(eip);
          }.bind(this, eip);
          return(
            <div className="truncate" style={{padding:"2px"}} 
                onClick={boundClick} key={eip.id}>
              <i className='fa fa-download btn btn-info btn-sm' /> &emsp;
              {eip.publicIp}
            </div>
          );
        }.bind(this));
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
        title="Lookup Elastic IPs">
        <div className="m-b-1">
          <Select ref="region" options={regionOptions} /> &emsp;
          <button className="btn btn-sm btn-primary" onClick={this.searchEips}>
            Search
          </button>
        </div>
        <div>
          {foundEips}
        </div>
      </BootstrapModal>
    );
  }
});

var EipAssign = React.createClass({
  getInitialState() {
    return {
      instances: []
    }
  },
  close() {
    this.refs.modal.close();
  },
  open() {
    this.loadDataFromServer();
    this.refs.modal.open();
  },
  assignEip() {
    this.props.updateParent();
  },
  loadDataFromServer() {
    $.ajax({
      url: "/api/admin/instances",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      dataType: 'json',
      cache: false,
      success: function(data) {
        console.debug(this.props);
        var niceData = data.filter(function(instance){
          return !instance.terminated && (instance.region === this.props.eip.region);
        }.bind(this));
        console.debug(niceData);
        this.setState({instances: niceData});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render(){
    var instanceOptions = this.state.instances.map(function(instance){
      return {name: instance.id + " : " + instance.description, value: instance.id};
    });
    return (
       <BootstrapModal
        ref="modal"
        onCancel={this.close}
        onConfrim={this.assignEip}
        confirm="Assign"
        title="Lookup Elastic IPs">
        <div className="m-b-1">
          Assign To: <Select ref="instance" options={instanceOptions} />
        </div>
        
      </BootstrapModal>
   );
  }
});

window.__APP__.EipPanel = React.createClass({
  getInitialState() {
    return {
      data:[],
      eipToAssign:null
    }
  },
  componentDidMount() {
    this.loadDataFromServer();
  },
  loadDataFromServer() {
    $.ajax({
      url: "/api/admin/eips",
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
  assign(item) {
    this.setState({
      eipToAssign: item
    });
    this.refs.assign.open();
  },
  //Callback for Accordian
  formatEipRow(item) {
    return(
      <div key={item.id} className="truncate">
        <i className="fa fa-link btn btn-sm btn-success m-r-1"
          onClick={this.assign.bind(this, item)}></i>
        <strong>{item.publicIp}: </strong> {item.instanceId ? item.instanceId : "not assigned"}
      </div>
    )
  },
  render() {
    var sorted = window.__APP__.sortByRegion(this.state.data);
    return(
      <div>
        <div className="m-b-1">
          <button className="btn btn-sm btn-primary" onClick={this.openLookup}>
            Lookup Elastic IPs
          </button>
        </div>
        {this.state.data.length == 0 ?
          <i className="fa fa-spinner fa-spin fa-2x"></i>
          :
          <div className="row">
            <div className="m-l-1">
            <Accordian id="region_list" 
              formatItemRow={this.formatEipRow}
              map={sorted} />
            </div>
          </div>
        }
        <EipLookup ref="lookup" 
          existing={this.state.data}
          awsConfig={this.props.awsConfig}
          updateParent={this.loadDataFromServer} />
        <EipAssign ref="assign"
          eip={this.state.eipToAssign}
          awsConfig={this.props.awsConfig}
          updateParent={this.loadDataFromServer} />
      </div>
    );
  }
});