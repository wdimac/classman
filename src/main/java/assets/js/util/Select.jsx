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
  onChange() {
    if (this.props.onChange)
      this.props.onChange();
  },
  getValue() {
    return this.state.current;
  },
  render() {
    return (
      <select ref='select' className="form-control m-b-1"
        defaultValue={this.props.myValue}
        style={{height:"2.3rem"}} onChange={this.onChange}>

        {this.props.options.map(function(item){
          return ( <Option item={item} key={item.value}/>)
        }.bind(this))}

      </select>
    );
  }
});