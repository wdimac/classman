var Option = React.createClass({
  render() {
    return(
      <option value={this.props.item.value} key={this.props.item.value}>
        {this.props.item.name}
      </option>
    );
  }
})

window.__APP__.Select = React.createClass({
  getValue() {
    return this.refs.select.value;
  },
  render() {
    return (
      <select ref='select' placeholder='Select a type' className="form-control m-b-1"
        style={{height:"2.3rem"}}>

        {this.props.options.map(function(item){
          return ( <Option item={item} />)
        })}

      </select>
    );
  }
});