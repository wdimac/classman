var BootstrapModal = window.__APP__.BootstrapModal;
var Select = window.__APP__.Select;
var Inliner = window.__APP__.Inliner;
var InlineSelect = window.__APP__.InlineSelect;

var Scheduler = React.createClass({
  getInitialState(){
    return {
      message:null
    }
  },
  close() {
    this.refs.modal.close();
  },
  schedule() {
    var classInfo = {
      classTypeDetail:{id:this.refs.type.getValue()},
      startDate:this.refs.date.value,
      startTime:this.refs.time.value + ":00",
      count:this.refs.count.value,
      timeZone:this.refs.zone.getValue()
    };
    console.debug(classInfo);
    $.ajax({
      url: "/api/admin/classes",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type:"POST",
      dataType: 'json',
      contentType: 'application/json',
      cache: false,
      data: JSON.stringify(classInfo),
      success: function(data) {
        this.props.updateParent();
        this.close();
      }.bind(this),
      error: function(xhr, status, err) {
        this.setState({message: "Failed to schedule class"});
        console.error(xhr, status, err.toString());
      }.bind(this)
    });

    this.refs.modal.close();
  },
  render() {
    var typeOptions= [];
    this.props.types.forEach(function(type){
      type.details.forEach(function(detail){
        if (detail.region)
          typeOptions.push({name:type.name + ": " + detail.region, value:detail.id});
      });
    });
    var zones = this.props.zones ? this.props.zones.map(function(zone){
      return {name:zone, value:zone}
    }) : [];
    return (
      <BootstrapModal
        ref="modal"
        onCancel={this.close}
        onConfirm={this.schedule}
        confirm="Schedule"
        message={this.state.message}
        title="Schedule Class">
        <div className="p-b-1">
          <Select ref="type" options={typeOptions} />
        </div>
        <div className="row">
          <div className="col-sm-6">
            <input ref="date" className="form-control m-b-1" 
                type="date" placeholder="Start Date" />
          </div>
          <div className="col-sm-6">
            <input ref="time" className="form-control m-b-1" 
                type="time" defaultValue="09:00" />
          </div>
        </div>
        <div className="p-b-1">
          <Select ref="zone" options={zones} />
        </div>
        <div className="input-group">
          <span className="input-group-addon"># Instances</span>
          <input ref="count" className="form-control" 
              type="number" defaultValue="6" />
        </div>
      </BootstrapModal>
    );
  }
});

var ClassInfo = React.createClass({
  getInitialState() {
    return {
      open:false
    }
  },
  toggleOpen(){
    this.setState({open:!this.state.open});
  },
  updateClass(cls){
    $.ajax({
      url: "/api/admin/classes/" + cls.id,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type:"PUT",
      dataType: 'json',
      contentType: 'application/json',
      cache: false,
      data: JSON.stringify(cls),
      success: function(data) {
        console.debug(data);
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
  },
  render() {
    var cl = this.props.info;
    var zoneOptions = this.props.zones.map(function(zone){
      return {name:zone, value:zone};
    })
    return (
      <div> 
        <div onClick={this.toggleOpen}>
          <span className="text-muted">
            {DateFormat.format.date(Date.parse(cl.startDate), "yyyy MMM d")}
          </span>
          <span className="m-l-1">
            {cl.classTypeDetail.classType.name}:&ensp;
          </span>
          <span className="text-muted">
            {cl.classTypeDetail.region}
          </span>
          {this.state.open ? 
            <i className="fa fa-minus-square-o m-l-1"></i>
            :
            <i className='fa fa-plus-square-o m-l-1'></i>
          }
        </div>
        <div id={this.props.panelName} 
            className={"panel-collapse m-b-1 p-x-2" + (this.state.open ? " open":" closed")}>
          <div className="row m-t-1">
            <div className="col-md-6 col-xs-12">
              <Inliner object={cl} field="description"
                  className=""
                  handleEdit={this.updateClass} />
            </div>
            <div className="col-xs-6">
              <Inliner object={cl} field="startDate" type="date"
                  className="m-x-1 pull-left" handleEdit={this.updateClass} />
              &nbsp;to&nbsp;
              <Inliner object={cl} field="endDate" type="date"
                  className="m-x-1 pull-left" handleEdit={this.updateClass} />
              <Inliner object={cl} field="startTime" type="time"
                  className="m-x-1 pull-left" handleEdit={this.updateClass} />
              &nbsp;to&nbsp;
              <Inliner object={cl} field="endTime" type="time"
                  className="m-x-1 pull-left" handleEdit={this.updateClass} />
            </div>
         </div>
          <div className="row m-t-1">
            <div className="col-md-6 col-xs-12">
              <InlineSelect object={cl} field="instructor" isObject="true"
                  options={this.props.instructors}
                  className="" handleEdit={this.updateClass} />
            </div>
            <div className="col-md-6 col-xs-12">
              <InlineSelect object={cl} field="timeZone"
                  options={zoneOptions}
                  className="" handleEdit={this.updateClass} />
            </div>
          </div>
          <div className="row m-t-1">
            <div className="col-md-6 col-xs-12">
              <Inliner object={cl} field="count" type="number"
                  className="" handleEdit={this.updateClass} />
            </div>
          </div>
        </div>
      </div>

    );
  }
});

window.__APP__.ClassesPanel = React.createClass({
  getInitialState() {
    return {
      loading:false,
      data:[],
      types:[],
      instructors:[]
    }
  },
  componentDidMount() {
    this.loadDataFromServer();
  },
  loadDataFromServer() {
    this.setState({loading:true});
    $.ajax({
      url: "/api/admin/classes",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      success: function(data) {
        data.sort(function(a,b){
          return Date.parse(a.startDate) - Date.parse(b.startDate);
        });
        this.setState({data: data, loading:false});
      }.bind(this),
      error: function(xhr, status, err) {
        this.setState({loading:false});
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
    // Load available types for scheduler
    $.ajax({
      url: "/api/admin/class_types",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      success: function(data) {
        console.debug(data);
        this.setState({types: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
    $.ajax({
      url: "/api/admin/users",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
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
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
  },
  sort(data) {
    data.sort
  },
  showDialog() {
    this.refs.scheduler.refs.modal.open();
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
                  onClick={this.showDialog}>
                Schedule New
              </button>
            </div>
              {this.state.data.map(function(cl){
                console.debug(cl);
                return (
                  <ClassInfo info={cl} key={cl.id} 
                    zones={this.props.awsConfig? this.props.awsConfig.timezones : []}
                    instructors={this.state.instructors}/>
                )
              }.bind(this))}
          </div>
        }
        <Scheduler ref="scheduler" types={this.state.types} 
            updateParent={this.loadDataFromServer}
            zones={this.props.awsConfig? this.props.awsConfig.timezones : []}/>
      </div>
    );
  }
});