var AccordianPanel = React.createClass({
	getInitialState() {
		return {
			open:false
		}
	},
	handleClick(event) {
		event.preventDefault();
		this.setState({open: !this.state.open});
	},
  render() {
    return (
		  <div className="panel panel-default">
		    <div className="panel-heading" role="tab" id={this.props.section}>
		      <h6 className={"panel-title"}>
		      	<span className="fa fa-stack">
		      		<i className="fa fa-minus fa-stack-1x"style={{paddingTop:"1px"}}></i>
		      	{this.state.open ? 
		      		<i className="fa fa-minus fa-stack-1x" style={{transition: "transform 0.5s", paddingTop:"1px"}}></i>
		      		:
		      		<i className='fa fa-minus fa-stack-1x fa-rotate-90' style={{transition: "transform 0.5s", marginLeft:"-1px"}}></i>
		      	}
		      	</span>
		        <a data-parent={"#" + this.props.id} 
		        	onClick={this.handleClick}
		        	href={"#" + this.props.panelName} aria-expanded="true" 
		        	aria-controls={this.props.panelName}>
							{this.props.section.replace(/__.*__/g, "")}
						</a>
					</h6>
				</div>
		    <div id={this.props.panelName} className={"panel-collapse m-b-1 " + (this.state.open ? " open":"closed")}
		    		role="tabpanel" aria-labelledby={this.props.section}>
					{this.props.itemRows}
				</div>
			</div>
    );
  }
});
window.__APP__.Accordian = React.createClass({
	render() {
		var rows = [];
		var myMap = this.props.map;
		var count = 0;
		for (var section in myMap) {
    	if (myMap.hasOwnProperty(section)) {
    		count += 1;
    		var itemRows = myMap[section].map(function(item){
    			return this.props.formatItemRow(item);
				}.bind(this));
				var panelName = "panel_" + count;
				rows.push(
				  <AccordianPanel key={count} id={this.props.id}
				  	itemRows={itemRows}
				  	section={section} panelName={panelName}/>
				)
    	}
    }

		return (
			<div id={this.props.id} role="tablist" aria-multiselectable="true">
				{rows}
			</div>
		);
	}
});