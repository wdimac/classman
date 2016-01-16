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
        <span className={this.props.className}>
          <span className="input-group">
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
        </span>
      )
    else
      return (
        <span className={"editable " + this.props.className} onClick={this.toggleEdit}>{
          text ? text : 
            <span className="placeholder">
              - {fieldName} -
            </span>
        }</span>
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
        <span className={this.props.className}>
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
        </span>
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
        <span className={"editable " + this.props.className} onClick={this.toggleEdit}>
          {label}
        </span>
      );
    }
  }
});