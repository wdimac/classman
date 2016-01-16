var Option = React.createClass({
  render() {
    return(
      <option value={this.props.item.value}>
        {this.props.item.name}
      </option>
    );
  }
})

window.__APP__.Select = React.createClass({
  onChange(event) {
    if (this.props.onChange)
      this.props.onChange(event);
  },
  getValue() {
    return this.refs.select.value;
  },
  render() {
    return (
      <select ref='select' className="form-control"
        value={this.props.myValue}
        style={{height:"2.3rem"}} onChange={this.onChange}>

        {this.props.options.map(function(item){
          return ( <Option item={item} key={item.value}/>)
        }.bind(this))}

      </select>
    );
  }
});