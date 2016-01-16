var Select = window.__APP__.Select;

window.__APP__.Inliner = React.createClass({
  getInitialState(){
    return{
      edit:false
    }
  },
  toggleEdit(){
    this.setState({edit:!this.state.edit});
  },
  doEdit() {
    this.props.object[this.props.field] = this.refs.myInput.value;
    if (this.props.handleEdit) {
      this.props.handleEdit(this.props.object);
    }
    this.toggleEdit();
  },
  render() {
    var text = this.props.object[this.props.field];
    var fieldName=window.__APP__.decamel(this.props.field);
    var clsnm="form-control";
    if (this.props.type === "date" || this.props.type === "time") {
      clsnm += " p-a-0";
    }
    if (this.state.edit)
      return (
        <div className={this.props.className}>
          <span className="input-group">
            { this.props.label ? 
              <span className="input-group-addon">
                {this.props.label}
              </span>
              : ""
            }
            <input ref="myInput" className={clsnm} placeholder={fieldName} 
                type={this.props.type ? this.props.type : "text"}
                defaultValue={text}/>
            <span className="input-group-btn">
              <button className="btn btn-secondary text-success" type="button"
                  onClick={this.doEdit}>
                <i className="fa fa-check" />
              </button>
              <button className="btn btn-secondary text-danger" type="button"
                  onClick={this.toggleEdit}>
                <i className="fa fa-times" />
              </button>
            </span>
          </span>
        </div>
      )
    else
      return (
        <div className={"editable " + this.props.className} onClick={this.toggleEdit}>
        { this.props.label ? 
          <label className="m-a-0">{this.props.label}&emsp;</label>
          : ""
        }
        {
          text ? text : 
            <span className="placeholder">
              - {fieldName} -
            </span>
        }
        </div>
      );
  }
});

window.__APP__.InlineSelect = React.createClass({
  getInitialState(){
    return{
      edit:false
    }
  },
  toggleEdit(){
    this.setState({edit:!this.state.edit});
  },
  doEdit(event) {
    this.props.object[this.props.field] = 
        (this.props.isObject === "true") ? {id:event.target.value} : event.target.value;
    if (this.props.handleEdit) {
      this.props.handleEdit(this.props.object);
    }
    this.toggleEdit();
  },
  render() {
    var object = this.props.object[this.props.field];
    var val = (this.props.isObject && object) ? object.id : object;
    var fieldName=window.__APP__.decamel(this.props.field);
    if (this.state.edit) {
      return (
        <div className={this.props.className}>
          <span className="input-group">
            <Select ref="myInput" options={this.props.options} 
                myValue={object ? object.id:null}
                onChange={this.doEdit} />
            <span className="input-group-btn">
              <button className="btn btn-secondary text-danger" type="button"
                  onClick={this.toggleEdit}>
                <i className="fa fa-times" />
              </button>
            </span>
          </span>
        </div>
      )
    } else {
      var label = (
        <span className="placeholder">
          - {fieldName} -
        </span>
      );
      if (object) {
        this.props.options.forEach(function(option){
          if (option.value && (option.value == object || option.value == object.id)) {
            label = option.name;
          }
        });
      }
      return (
        <div className={"editable " + this.props.className} onClick={this.toggleEdit}>
          {label}
        </div>
      );
    }
  }
});

window.__APP__.DatePicker = React.createClass({
  getInitialState(){
    return{
      edit:false
    }
  },
  toggleEdit(){
    this.setState({edit:true});
    $("#" + this.props.object.id + "_dp")
      .datepicker("dialog", 
        new Date(this.props.object[this.props.field]),
        this.doEdit,
        {
          dateFormat:'yy-mm-dd',
          minDate:1,
          showButtonPanel:true
        }
      );
  },
  doEdit(date, picker) {
    this.props.object[this.props.field] = date;
    if (this.props.handleEdit) {
      this.props.handleEdit(this.props.object);
    }
    this.setState({edit:false});
  },
  render() {
    var text = this.props.object[this.props.field];
    var fieldName=window.__APP__.decamel(this.props.field);
    var clsnm="form-control";
    if (this.props.type === "date" || this.props.type === "time") {
      clsnm += " p-a-0";
    }
    return (
      <div className={"editable " + this.props.className} 
          onClick={this.toggleEdit}>
        { text ? text : 
            <span className="placeholder">
              - {fieldName} -
            </span>
        }
        <i className="fa fa-calendar m-l-1"></i>
        <div id={this.props.object.id + "_dp"}></div>
      </div>
    );
  }
});
