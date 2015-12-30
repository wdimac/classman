window.__APP__.Accordian = React.createClass({
	render() {
		var rows = [];
		var myMap = this.props.map;
		for (var section in myMap) {
    	if (myMap.hasOwnProperty(section)) {
    		var itemRows = myMap[section].map(function(item){
    			return this.props.formatItemRow(item);
				}.bind(this));
				var panelName = section + '_panel';
				rows.push(
				  <div className="panel panel-default" key={section}>
				    <div className="panel-heading" role="tab" id={section}>
				      <h6 className="panel-title">
				        <a data-toggle="collapse" data-parent={"#" + this.props.id}  
			        		href={"#" + panelName} aria-expanded="true" aria-controls={panelName}>
									{section}
								</a>
							</h6>
						</div>
				    <div id={panelName} className="panel-collapse collapse m-b-1" 
				    		role="tabpanel" aria-labelledby={section}>
							{itemRows}
						</div>
					</div>
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