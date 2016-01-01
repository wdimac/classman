var BootstrapModal = window.__APP__.BootstrapModal;

window.__APP__.Launcher = React.createClass({
  getInitialState() {
    return {
      active:true,
      message:null
    }
  },
	open() {
		this.refs.modal.open();
	},
	close() {
		this.refs.modal.close();
	},
	runEm() {
    if (this.state.active) {
      this.setState({active:false, message:null});

  		var url = '/api/admin/images/' + this.props.target.id + '/run';
  		$.ajax({
        url: url,
        headers: {'X-AUTH-TOKEN':Auth.getToken()},
        type: 'POST',
        dataType: 'json',
        data:{type:this.refs.type.value, count: this.refs.count.value},
        cache: false,
        success: function(data) {
        	console.debug(data);
          this.setState({active:true, message: "Successfully started " + data.length + " instance(s)."});
        }.bind(this),
        error: function(xhr, status, err) {
          this.setState({active:true, message: "Failed to start instances."});
          console.error("DELETE " + url, status, err.toString());
        }.bind(this)
      });
    }
	},
	render() {
		var options = "";
		if (this.props.awsConfig.instanceTypes){
			options = this.props.awsConfig.instanceTypes.map(function(type) {
				return (
					<option value={type} key={type}>{type}</option>
				);
			});
		}
		var typeSelect = (
			<select ref='type' placeholder='Select a type' className="form-control m-b-1"
				style={{height:"2.3rem"}}>
			{options}
			</select>
		);
		return(
			<BootstrapModal
        ref="modal"
        onCancel={this.close}
        onConfirm={this.runEm}
        confirm="Run instances"
        title="Run Instances"
        disable={!this.state.active}
        message={this.state.message}>
        <form id="launch_form">
	        <div className="row m-b-1">
	        	<div className="col-sm-6">
	        		<strong>For class:</strong> TBD
	        	</div>
	        	<div className="col-sm-6">
	        		<strong>AMI ID:</strong> {this.props.target.id}
	        	</div>
	        </div>
	        <div className="row">
	        	<div className="col-sm-6">
	        		{typeSelect}
	        	</div>
	        	<div className="col-sm-6 instance_count">
	        		<input ref="count" type="number" className="form-control"
	        				placeholder="# Instances"/>
	        	</div>
	        </div>
        </form>
      </BootstrapModal>

		);
	}
});