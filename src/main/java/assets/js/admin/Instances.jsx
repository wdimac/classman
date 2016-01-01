var Accordian = window.__APP__.Accordian;
var PropsSpewer = window.__APP__.PropsSpewer;

//Detail panel for specific instance
var InstanceDetailPanel = React.createClass({
  getInitialState() {
    return {
      detailInstance:null,
      processing:false
    }
  },
  loadDataFromServer() {
    if (!this.props.instance.id || this.props.instance.terminated) return;

    var url = "/api/admin/aws/"
      + this.props.instance.region + '/instances/' + this.props.instance.id;
    $.ajax({
      url: url,
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({detailInstance: data});
        // Reload if not one of the following
        if (data.state.name !== 'stopped' 
          && data.state.name !== 'runninng'
          && data.state.name !== 'terminated')
          setTimeout(this.loadDataFromServer, 3000);
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.instance.id, status, err.toString());
      }.bind(this)
    });
  },
  componentDidMount() {
    this.loadDataFromServer();
  },
  componentWillReceiveProps(newProps) {
    if (newProps.instance.id !== this.props.instance.id) {
      this.setState({
        detailInstance:null
      })
    }
  },
  componentDidUpdate(prevProps, prevState) {
    if (!this.state.detailInstance) {
      this.loadDataFromServer();
    }
  },
  changeState(action) {

    var url = "/api/admin/aws/"
      + this.props.instance.region 
      + '/instances/' + this.props.instance.id
      + '/' + action;
    $.ajax({
      url: url,
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      type: 'POST',
      dataType: 'json',
      cache: false,
      success: function(data) {
        console.debug(data);
        this.setState({processing: false});
        this.loadDataFromServer();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.instance.id, status, err.toString());
      }.bind(this)
    });
  },
  startUp() {
    this.changeState("start");
  },
  shutDown() {
    this.changeState("stop");
  },
  terminate() {
    if (confirm('Really terminate this instance?')) {
      this.changeState("terminate");
      this.props.instance.terminated = true;
    }
  },
  render() {
    if (this.props.instance.id) {
      var power = ' text-muted';
      var powerC = function(){};
      var term = ' text-black';
      var termC = this.terminate;
      if (this.state.processing) {
        term = ' text-muted';
        termC = powerC;
      } else {
        if (this.props.instance.terminated) {
          term = ' text-danger';
          termC = powerC;
        } else if (this.state.detailInstance) {
          if (this.state.detailInstance.state.name === 'running') {
            power = ' text-success';
            powerC = this.shutDown;
          } else if (this.state.detailInstance.state.name === 'stopped'){
            power = ' text-danger';
            powerC = this.startUp;
          } else {
            power += ' fa-spin';
            term = ' text-muted';
            termC = powerC;
          }
        }
      }
      var detail = "";
      if (this.state.detailInstance) {
        detail = (
          <PropsSpewer 
            item={this.state.detailInstance}
            omit={["instanceId"]}/>
        );
      } else {
        if (this.props.instance.terminated) {
          detail = (
            <div>
              <strong>Instance is defunct.</strong>
            </div>
          )
       } else {
          detail = (
            <div>
              <i className="fa fa-spinner fa-spin fa-2x fa-pull-left"></i>
              Retreiving details from AWS
            </div>
          )
        }
      }
            
      // Render
      return (
        <div className="card">
          <div className="card-header bg-info">
            <div className="pull-right">
              <i className={"fa fa-lg fa-power-off btn btn-sm" + power}
                  onClick={powerC}></i>
              <i className={"fa fa-lg fa-trash btn btn-sm" + term}
                  onClick={termC}></i>
            </div>
            Instance: {this.props.instance.id}
          </div>
          <div className="card-body p-a-1">
            <div>
              {this.props.instance.description}
            </div>
            {detail}
          </div>
        </div>
      );
    } else {
      return (
        <div></div>
      );
    }
  }
})

//Main instances panel
window.__APP__.Instances = React.createClass({
  loadDataFromServer() {
    this.setState({loading:true});
    $.ajax({
      url: "/api/admin/instances",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data, loading:false});
      }.bind(this),
      error: function(xhr, status, err) {
        this.setState({data: [], loading:false});
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  getInitialState() {
    return {
      loading:false,
      data: [],
      detailItem:{}
    };
  },
  componentDidMount() {
    this.loadDataFromServer();
  },
  formatInstanceRow(instance) {
    var color = " bg-info";
    if (instance.terminated)
      color = " bg-faded";
    return (
      <div key={instance.id} className="truncate" >
        <i className={'fa fa-file-text btn btn-sm m-r-1' + color}
          onClick={this.displayDetail.bind(this, instance)}></i>
        <strong>{instance.id}: </strong>
        {instance.description}
      </div>
    );
  },
  displayDetail(instance) {
    this.setState({detailItem: instance});
  },
	render() {
    var sorted = window.__APP__.sortByRegion(this.state.data);
		return (
			<div>
      { this.state.loading ?
        <i className="fa fa-spinner fa-spin fa-2x"></i>
        :
        <div className="row">
          <div className="col-sm-6">
            <Accordian id="region_list" 
              formatItemRow={this.formatInstanceRow}
              map={sorted} />
          </div>
          <div className="col-sm-6">
            <InstanceDetailPanel instance={this.state.detailItem} />
          </div>
        </div>
      }
      </div>
		);
	}
});