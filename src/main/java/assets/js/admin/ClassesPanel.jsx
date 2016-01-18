var BootstrapModal = window.__APP__.BootstrapModal;
var Select = window.__APP__.Select;
var Inliner = window.__APP__.Inliner;
var InlineSelect = window.__APP__.InlineSelect;
var DatePicker = window.__APP__.DatePicker;

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
      open:false,
      infos:null
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
        this.props.updateParent();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
  },
  deleteClass() {
    $.ajax({
      url: "/api/admin/classes/" + this.props.clazz.id,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type:"DELETE",
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.props.updateParent();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
  },
  launch(count) {
    $.ajax({
      url: "/api/admin/classes/" + this.props.clazz.id + "/instances?count=" + count,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type:"POST",
      dataType: 'json',
      cache: false,
      success: function(data) {
        if (this.props.clazz.instances) {
          this.props.clazz.instances.concat(data);
        } else {
          this.props.clazz.instances = data;
        }
        this.props.updateParent();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
  },
  changeAll(state) {
    $.ajax({
      url: "/api/admin/classes/" + this.props.clazz.id + "/aws/" + state,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type:"POST",
      dataType: 'json',
      cache: false,
      success: function(data) {
        if (state === "TERMINATE") {
          this.props.clazz.instances.forEach(function(inst){
            inst.terminated = true;
          });
        }
        this.getInfo();
       }.bind(this),
      error: function(xhr, status, err) {
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
  },
  getInfo(){
    $.ajax({
      url: "/api/admin/classes/" + this.props.clazz.id + "/aws",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type:"GET",
      dataType: 'json',
      cache: false,
      success: function(data) {
        var infos={};
        var again = false;
        data.forEach(function(info){
          infos[info.instanceId] = info;
          if (['stopped','running','terminated'].indexOf(info.state.name) <0) {
            again=true;
          }
        });
        this.setState({infos:infos});
        if (again) {
          setTimeout(this.getInfo, 3000);
        }
       }.bind(this),
      error: function(xhr, status, err) {
        console.error(xhr, status, err.toString());
      }.bind(this)
    });
  },
  render() {
    var cl = this.props.clazz;
    var zoneOptions = this.props.zones ? this.props.zones.map(function(zone){
      return {name:zone, value:zone};
    }):[];
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
              <div className="row m-t-1">
                <div className="col-xs-12">
                  <Inliner object={cl} field="description"
                      className=""
                      handleEdit={this.updateClass} />
                </div>
              </div>
              <div className="row m-t-1">
                <div className="col-xs-12">
                  <InlineSelect object={cl} field="instructor" isObject="true"
                      options={this.props.instructors}
                      className="" handleEdit={this.updateClass} />
                </div>
              </div>
              <div className="row m-t-1">
                <div className="col-xs-12">
                  <Inliner object={cl} field="count" type="number"
                      label="# Instances"
                      className="" handleEdit={this.updateClass} />
                </div>
              </div>
            </div>

            <div className="col-md-6 col-xs-12">
              <div className="row m-t-1">
                <div className="col-xs-6">
                  <DatePicker object={cl} field="startDate"
                      className="" handleEdit={this.updateClass} />
                </div>
                <div className="col-xs-6">
                  <DatePicker object={cl} field="endDate"
                      className="" handleEdit={this.updateClass} />
                </div>
              </div>
              <div className="row m-t-1">
                <div className="col-xs-6">
                   <Inliner object={cl} field="startTime" type="time"
                       className="" handleEdit={this.updateClass} />
                </div>
                <div className="col-xs-6">
                   <Inliner object={cl} field="endTime" type="time"
                       className="" handleEdit={this.updateClass} />
                </div>
              </div>
              <div className="row m-t-1">
                <div className="col-xs-12">
                  <InlineSelect object={cl} field="timeZone"
                      options={zoneOptions}
                      className="" handleEdit={this.updateClass} />
                </div>
              </div>
            </div>
          </div>
          <div className="m-t-1 text-xs-center clearfix"> {/*Button Panel*/}
            <button className="btn btn-sm btn-danger pull-right"
                title="Remove Class"
                onClick={this.deleteClass}>
              <i className="fa fa-times"></i>
            </button>
            <div className="btn-group pull-left">
              <button className="btn btn-sm btn-secondary"
                  title="Sync Info"
                  onClick={this.getInfo}>
                <i className="fa fa-cloud-download"> All</i>
              </button>
              <button className="btn btn-sm btn-success-outline"
                  title="Start All"
                  onClick={this.changeAll.bind(this, "START")}>
                <i className="fa fa-power-off"> All</i>
              </button>
              <button className="btn btn-sm btn-danger-outline"
                  title="Stop All"
                  onClick={this.changeAll.bind(this, "STOP")}>
                <i className="fa fa-power-off"> All</i>
              </button>
              <button className="btn btn-sm btn-warning"
                  title="Terminate All"
                  onClick={this.changeAll.bind(this, "TERMINATE")}>
                <i className="fa fa-trash"> All</i>
              </button>
            </div>
            <div className="btn-group ">
              <button className="btn btn-sm btn-primary-outline"
                  title="Launch All"
                  onClick={this.launch.bind(this, 0)}>
                <i className="fa fa-rocket"> All</i>
              </button>
              <button className="btn btn-sm btn-primary-outline"
                  title="Launch One Instance"
                  onClick={this.launch.bind(this, 1)}>
                <i className="fa fa-rocket"> 1</i>
              </button>
            </div>          
          </div> {/*End button panel*/}
          <div className="m-t-1">
            {cl.instances.map(function(inst){
              return(<InstanceRow key={inst.id} inst={inst} 
                      updateParent={this.getInfo}
                      info={this.state.infos ? this.state.infos[inst.id]:null} />)
            }.bind(this))}
          </div>
        </div>
      </div>
    );
  }
});

var InstanceRow = React.createClass({
  changeState(action) {
    if (!action) return;

    var url = "/api/admin/aws/"
      + this.props.inst.region 
      + '/instances/' + this.props.inst.id
      + '/' + action;
    $.ajax({
      url: url,
      headers: { 'X-AUTH-TOKEN':Auth.getToken() },
      type: 'POST',
      dataType: 'json',
      cache: false,
      success: function(data) {
        console.debug("action=" + action);
        if (action === "TERMINATE") this.props.inst.terminated = true;
        this.props.updateParent();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.instance.id, status, err.toString());
      }.bind(this)
    });
  },
  render() {
    var icon=" fa-circle-o-notch"
    var btn=" btn-secondary";
    var newState=null;
    if (this.props.info) {
      console.debug(this.props.info);
      if (this.props.info.state.name === 'running'){
        icon="fa-power-off";
        btn="btn-success";
        newState="STOP";
      } else if (this.props.info.state.name === 'stopped') {
        icon="fa-power-off"
        btn="btn-danger";
        newState="START";
      } else {
        icon="fa-power-off fa-spin";
        btn="btn-secondary";
      }
    }
    return (
      <div className="m-b-1"> 
      { this.props.inst.terminated ?
        <div className="btn-group btn-group-sm m-r-1">
          <button className="btn btn-warning-outline">
            <i className="fa fa-trash"></i>
          </button>
        </div>
        :
        <div className="btn-group btn-group-sm m-r-1">
          <button className={"btn " + btn} 
            onClick={this.changeState.bind(this, newState)}>
            <i className={"fa " + icon}></i>
          </button>
          <button className="btn btn-warning"
            onClick={this.changeState.bind(this, 'TERMINATE')}>
            <i className="fa fa-trash"></i>
          </button>
         </div>
      }
      {this.props.inst.id} : {this.props.inst.description} 
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
    if (this.state.data.length === 0) {
      this.setState({loading:true});
    }
    
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
              return (
                <ClassInfo clazz={cl} key={cl.id} 
                  updateParent={this.loadDataFromServer}
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