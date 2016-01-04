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
    this.setState({regionEips:[]});
    this.refs.modal.open();
  },
  close() {
    this.refs.modal.close();
  },
  searchEips() {
    // redo search if we have switched regions
    if (this.refs.region.getValue() !== this.state.currentModalRegion) {
      this.setState({searchState:"searching"});
      $.ajax({
        url: "/api/admin/aws/" + this.refs.region.getValue() + "/eips",
        headers: {'X-AUTH-TOKEN':Auth.getToken()},
        dataType: 'json',
        cache: false,
        success: function(data) {
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
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      contentType: 'application/json',
      dataType: 'json',
      data:JSON.stringify({
        publicIp: eip.publicIp, 
        description: eip.publicIp, 
        region: this.refs.region.getValue(),
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
          <Select ref="region" options={regionOptions} />
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
    var url= "/api/admin/eips/" + this.props.eip.id + "?instanceId=" + this.refs.instance.getValue();
    $.ajax({
      url: url,
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
      },
      type: "PUT",
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.props.updateParent();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  loadDataFromServer() {
    $.ajax({
      url: "/api/admin/instances",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      success: function(data) {
        var niceData = data.filter(function(instance){
          return !instance.terminated && (instance.region === this.props.eip.region);
        }.bind(this));
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
        onConfirm={this.assignEip}
        confirm="Assign"
        title="Lookup Elastic IPs">
        <div className="m-b-1">
          Assign To: <Select ref="instance" options={instanceOptions} />
        </div>
        
      </BootstrapModal>
   );
  }
});

var EipAllocate = React.createClass({
  close() {
    this.refs.modal.close();
  },
  open() {
    this.refs.modal.open();
  },
  allocateEip() {
    var url= "/api/admin/aws/" + this.refs.region.getValue() + "/eips" 
               + (this.refs.vpc.checked ? "?vpc=true":"");
    $.ajax({
      url: url,
      headers: { 'X-AUTH-TOKEN':Auth.getToken() },
      type: "POST",
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.props.updateParent();
        this.refs.modal.close();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render() {
    var regionOptions = this.props.awsConfig.regions ?
      this.props.awsConfig.regions.map(function(region) {
        return {value:region[0], name:region[1]};
      }):[];
    return (
      <BootstrapModal
        ref="modal"
        onCancel={this.close}
        onConfirm={this.allocateEip}
        confirm="Create Eip"
        title="Create Elastic IP">
        <div className="m-b-1">
          <Select ref="region" options={regionOptions} />
          <strong>VPC</strong> <input type="checkbox" ref="vpc" />
        </div>
        
      </BootstrapModal>
    );
  }
}); //EipAllocate

window.__APP__.EipPanel = React.createClass({
  getInitialState() {
    return {
      data:[],
      eipToAssign:null,
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
      url: "/api/admin/eips",
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
  assign(item) {
    this.setState({
      eipToAssign: item
    });
    this.refs.assign.open();
  },
  allocateEip() {
    this.refs.alloc.open();
  },
  del(item) {
    if (!this.state.deleting) {
      this.setState({deleting:item});
      $.ajax({
        url: "/api/admin/eips/" + item.id,
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
  formatEipRow(item) {
    var delThis = this.state.deleting && (item.id == this.state.deleting.id);
    return(
      <div key={item.id} className="truncate">
        <i className="fa fa-link btn btn-sm btn-success m-r-1"
          onClick={this.assign.bind(this, item)}></i>
        <i className={"fa btn btn-sm btn-danger m-r-1 " + (delThis ? "fa-hourglass-half" : "fa-times")}
          onClick={this.del.bind(this, item)}></i>
        <strong>{item.publicIp}: </strong> {item.instanceId ? item.instanceId : "not assigned"}
      </div>
    )
  },
  render() {
    var sorted = window.__APP__.sortByRegion(this.state.data);
    return(
      <div>
        <div className="m-b-1">
          <button className="btn btn-sm btn-success m-r-1" onClick={this.allocateEip}>
            Allocate Elastic IP
          </button>
          <button className="btn btn-sm btn-primary" onClick={this.openLookup}>
            Search for Elastic IPs
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
        <EipAllocate ref="alloc"
          awsConfig={this.props.awsConfig}
          updateParent={this.loadDataFromServer} />
      </div>
    );
  }
});