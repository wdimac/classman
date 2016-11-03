var BootstrapModal = window.__APP__.BootstrapModal;
var Select = window.__APP__.Select;

window.__APP__.Launcher = React.createClass({
  getInitialState() {
    return {
      active:true,
      message:null,
      groups:[]
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
        data:{
          type:  this.refs.type.getValue(), 
          count: this.refs.count.value,
          group: this.refs.group.getValue()
        },
        cache: false,
        success: function(data) {
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
		var typeOptions = this.props.awsConfig.instanceTypes.map(function(type) {
      return {name:type, value:type};
    });
    
    var securityOptions = this.props.groups.filter(function(group){
      return this.props.target.region === group.region;
    }.bind(this)).map(function(group){
      return {name: group.name, value: group.id, defunct: group.defunct};
    })

    if (securityOptions.length ===0) {
      securityOptions = [{name:"No Security Groups Available!", value:""}];
    }

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
	        		<Select ref="type" options={typeOptions} />
	        	</div>
	        	<div className="col-sm-6 instance_count">
	        		<input ref="count" type="number" className="form-control"
	        				placeholder="# Instances"/>
	        	</div>
	        </div>
          <div className="row">
            <div className="col-sm-6">
              <Select ref="group" options={securityOptions} />
            </div>
            <div className="col-sm-6">

            </div>
          </div>
        </form>
      </BootstrapModal>

		);
	}
});