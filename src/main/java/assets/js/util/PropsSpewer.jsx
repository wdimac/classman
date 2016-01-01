var PropsSpewer = React.createClass({
  render() {
    var rows = [];
    for (var key in this.props.item) {
      if (this.props.item.hasOwnProperty(key)
          && this.props.omit.indexOf(key) < 0) {
        var name = window.__APP__.decamel(key);
        if (typeof this.props.item[key] === 'object') {
          var children= (
            <PropsSpewer item={this.props.item[key]} omit={this.props.omit} />
          );
          rows.push(
            <div key={key}>
              <strong>{name}:</strong>
              <div className="p-l-1">
                {children}
              </div>
            </div>
          )
        } else {
          rows.push(
            <div key={key}>
              <strong>{name}: </strong>
              {this.props.item[key]}
            </div>
          )
        }
      }
    }
    return(
      <div>
        {rows}
      </div>
    );
  }
});
window.__APP__.PropsSpewer = PropsSpewer;