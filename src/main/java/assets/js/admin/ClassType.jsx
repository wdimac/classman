var Accordian = window.__APP__.Accordian;
var Select = window.__APP__.Select;

var DetailRow = React.createClass({
  dropDetail(detail) {
    $.ajax({
      url: "/api/admin/class_types/" + detail.classType + "/details/" + detail.id,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type: 'DELETE',
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
  markDirty(type, event) {
    var detail = this.props.detail;
    switch (type) {
      case 'region':
        detail.region = event.target.value; break;
      case 'type':
        detail.instanceType = event.target.value; break;
      case 'group':
        detail.securityGroupId = event.target.value; break;
      case 'image':
        detail.imageId = event.target.value; break;
    }
    $.ajax({
      url: "/api/admin/class_types/" + detail.classType + "/details/" + detail.id,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type: 'PUT',
      dataType: 'json',
      contentType: 'application/json',
      cache: false,
      data: JSON.stringify(detail),
      success: function(data) {
        this.props.updateParent();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render() {
    var deflt = [{name:"Select", value:""}];
    var regionOptions=deflt.concat(
      this.props.selects.regions.map(function(item) {
        return {name:item[1], value: item[0]};
      })
    )
    var typeOptions=deflt.concat(
      this.props.selects.types.map(function(type){
        return {name:type, value:type};
      })
    )
    var imageOptions = deflt;
    var groupOptions = deflt;
    if (this.props.detail.region != null) {
      imageOptions= imageOptions.concat(
        this.props.selects.images.filter(function(image){
          return image.region === this.props.detail.region;
        }.bind(this)).map(function(image) {
          return {name:image.description, value:image.id};
        })
      );
      groupOptions=groupOptions.concat(
        this.props.selects.groups.filter(function(group){
          return group.region === this.props.detail.region;
        }.bind(this)).map(function(group) {
          return {name:group.name, value:group.id};
        })
      );
    }
    return (
      <div className="card " style={{minWidth:"250px",padding:"0.5rem"}}>
        <div>
          <Select ref="region" options={regionOptions} myValue={this.props.detail.region}
            onChange={this.markDirty.bind(this, "region")} />
        </div>
        <div>
          <Select ref="images" options={imageOptions} myValue={this.props.detail.imageId}
            onChange={this.markDirty.bind(this, "image")} />
        </div>
        <div>
          <Select ref="types" options={typeOptions} myValue={this.props.detail.instanceType}
            onChange={this.markDirty.bind(this, "type")} />
        </div>
        <div>
          <Select ref="groups" options={groupOptions} myValue={this.props.detail.securityGroupId}
            onChange={this.markDirty.bind(this, "group")} />
        </div>
        <div>
          <div className="btn-group">
          <i className="fa fa-times btn btn-sm btn-danger"
              onClick={this.dropDetail.bind(this, this.props.detail)}></i>
          </div>
        </div>
      </div>
    );
  }
});

window.__APP__.ClassTypePanel = React.createClass({
  getInitialState(){
    return {
      data: [],
      waiting: [],
      selects: {
        regions: [],
        types:   [],
        images:  [],
        groups:  []
      }
    }
  },
  componentDidMount() {
    this.loadOther();
    this.loadDataFromServer();
    this.setSelects(this.props);
  },
  removeWait(item) {
    var idx = this.state.waiting.indexOf(item);
    this.state.waiting.splice(idx, 1);
  },
  loadDataFromServer() {
    this.state.waiting.push("data")
    $.ajax({
      url: "/api/admin/class_types",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      async:false,
      success: function(data) {
        this.removeWait("data");
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        this.removeWait("data");
        this.state.waiting.splice(idx, 1);
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  loadOther() {
    this.state.waiting.push("images");
    $.ajax({
      url: "/api/admin/images",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      timeout: 5000,
      async:false,
      success: function(data) {
        this.removeWait("images");
        this.state.selects.images= data;
      }.bind(this),
      error: function(xhr, status, err) {
        this.removeWait("images");
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
    this.state.waiting.push("groups");
    $.ajax({
      url: "/api/admin/security_groups",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      timeout: 5000,
      async:false,
      success: function(data) {
        this.removeWait("groups");
        this.state.selects.groups = data;
      }.bind(this),
      error: function(xhr, status, err) {
        this.removeWait("groups");
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  componentWillReceiveProps(newProps) {
    this.setSelects(newProps);
  },
  setSelects(newProps) {
    if (newProps.awsConfig.instanceTypes) {
      this.state.selects.types = newProps.awsConfig.instanceTypes;
    }
    if (newProps.awsConfig.regions) {
      this.state.selects.regions = newProps.awsConfig.regions;
    }
  },
  addType() {
    $.ajax({
      url: "/api/admin/class_types",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type: 'POST',
      dataType: 'json',
      contentType: 'application/json',
      cache: false,
      data: JSON.stringify({name:"New class type", duration:4}),
      success: function(data) {
        this.loadDataFromServer();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  addDetail(forType) {
   $.ajax({
      url: "/api/admin/class_types/" + forType.id + "/details",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type: 'POST',
      dataType: 'json',
      contentType: 'application/json',
      cache: false,
      data: JSON.stringify({}),
      success: function(data) {
        this.loadDataFromServer();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });    
  },
  updateType(type) {
    type.name = $("#name-" + type.id).val(), 
    type.duration = parseInt($("#duration-" + type.id).val()),
    $.ajax({
      url: "/api/admin/class_types/" + type.id,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type: 'PUT',
      dataType: 'json',
      contentType: 'application/json',
      cache: false,
      data: JSON.stringify(type),
      success: function(data) {
        var update = this.state.data.map(function(type){
          if (type.id === data.id) {
            return data;
          } else {
            return type;
          }
        });
        this.setState({data:update});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  deleteType(type) {
    $.ajax({
      url: "/api/admin/class_types/" + type.id,
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      type: 'DELETE',
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.loadDataFromServer();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  formatType(type) {
    return (
      <div className="p-x-1" key={type.id}>
        <div className="row bg-faded p-y-1">
          <div className="col-sm-6">
            <input id={"name-" + type.id} defaultValue={type.name} className="form-control"
                onChange={this.updateType.bind(this, type)}/>
          </div>
          <div className="col-sm-4">
            <input id={"duration-" + type.id} defaultValue={type.duration} type="number" 
                className="form-control m-r-1 text-xs-right"
                style={{width:"50%", display:"inline"}}
                onChange={this.updateType.bind(this, type)}/>
            Days
          </div>
          <div className="col-sm-2">
            <i className="fa fa-times fa-lg btn btn-danger"
                onClick={this.deleteType.bind(this, type)}></i>
          </div>
        </div>
        <div className="p-y-1" style={{ whiteSpace:"nowrap",overflowX:"scroll"}}>
          <div className="card-deck">
            { type.details ?
              type.details.map(function(detail) {
                return (<DetailRow key={detail.id} detail={detail} 
                  updateParent={this.loadDataFromServer}
                  selects={this.state.selects}/>)
              }.bind(this))
              : ""
            }
            <div className="card" style={{border:"0"}}>
              <button className="btn btn-sm btn-success m-b-1"
                  onClick={this.addDetail.bind(this, type)}>
                <i className="fa fa-plus fa-inverse"></i> &ensp; New
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  },
  render() {
    var sorted = {};
    this.state.data.forEach(function(item){
      sorted[item.name + "__" + item.id + "__"] = [item];
    });
    return (
      <div>
        <div>
          <button className="btn btn-sm btn-success m-b-1"
              onClick={this.addType}>
            <i className="fa fa-plus fa-inverse"></i> New
          </button>
        </div>
        { this.state.waiting.length > 0 ?
          <div>
            <i className="fa fa-lg fa-spin fa-spinner"></i>
            Retrieving data from server...
          </div>
          :
          <div>
            <Accordian id="type_list" 
              formatItemRow={this.formatType}
              map={sorted} />
          </div>
        }
      </div>
    );
  }
});