var BootstrapModal = window.__APP__.BootstrapModal;

var ImageDetail = React.createClass({
	getInitialState() {
		return {
			detailImage:null
		}
	},
	loadDataFromServer() {
		var url = "/api/admin/aws/images/"
			+ this.props.image.region + '/' + this.props.image.id;
		$.ajax({
      url: url,
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      dataType: 'json',
      cache: false,
      success: function(data) {
      	this.setState({detailImage: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.image.id, status, err.toString());
      }.bind(this)
    });
	},
	componentDidMount() {
		this.loadDataFromServer();
	},
	componentWillReceiveProps(newProps) {
		if (newProps.image.id !== this.props.image.id) {
			this.setState({
				detailImage:null
			})
		}
	},
	componentDidUpdate(prevProps, prevState) {
		if (!this.state.detailImage) {
			this.loadDataFromServer();
		}
	},
	render() {
		console.debug(this.state.detailImage);
		return (
			<div className="card">
				<div className="card-header bg-info">
					Image: {this.props.image.id}
				</div>
				<div className="card-body" style={{padding:"1rem"}}>
					<div><strong>AMI Name: </strong> {this.props.image.description}</div>
					{this.state.detailImage ?
						<div>
							<div>
								<strong>Owner:</strong> {this.state.detailImage.ownerId}
							</div>
							<div>
								<strong>Status:</strong> {this.state.detailImage.state}
							</div>
							<div>
								<strong>Platform:</strong> {this.state.detailImage.platform}
							</div>
							<div>
								<strong>Root Device Type:</strong> {this.state.detailImage.rootDeviceType}
							</div>
							<div>
								<strong>Virtualization:</strong> {this.state.detailImage.virtualizationType}
							</div>
						</div>
						:
						<span>
							<i className="fa fa-spinner fa-spin fa-2x"></i>
							Retreiving details from AWS
						</span>
					}
				</div>
			</div>
		)
	}
});
window.__APP__.Images = React.createClass({
	getInitialState() {
		return {
			data:[],
			regionImages: [],
			modalState:"waiting",
			currentModalRegion:"",
			detailItem:null
		}
	},
  loadDataFromServer() {
  	$.ajax({
      url: "/api/admin/images",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      dataType: 'json',
      cache: false,
      success: function(data) {
      	this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  addImage(image) {
  	$.ajax({
  		url: "/api/admin/images",
  		type: "POST",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      dataType: 'json',
      data:JSON.stringify({id: image.imageId, description: image.name, region: this.refs.region.value}),
      cache: false,
      success: function(data) {
      	this.loadDataFromServer();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
  	});
  },
  componentDidMount() {
    this.loadDataFromServer();
  },	
  openModal() {
  	this.refs.modal.open();
  },
  onCancel() {
  	this.refs.modal.close();
  },
  searchImages() {
  	if (this.refs.region.value !== this.state.currentModalRegion) {
	  	this.setState({modalState:"searching"});
	  	$.ajax({
	      url: "/api/admin/aws/images/" + this.refs.region.value,
	      headers: {'X-AUTH-TOKEN':Auth.getToken()},
	      dataType: 'json',
	      cache: false,
	      success: function(data) {
	      	this.setState({
	      		regionImages: data, 
	      		modalState:"waiting", 
	      		currentModalRegion: this.refs.region.value
	      	});
	      }.bind(this),
	      error: function(xhr, status, err) {
	        console.error(this.props.url, status, err.toString());
	      }.bind(this)
	    });
	  }
  },
  viewDetail(item) {
  	this.setState({detailItem:item});
  },
	render() {
		var cRegion = "";
		var sorted = {};
		this.state.data.forEach(function(item){
			if (item.region !== cRegion) {
				cRegion = item.region;
				sorted[cRegion] = [];
			}
			sorted[cRegion].push( item);
		})
		var rows = [];
		for (var region in sorted) {
    	if (sorted.hasOwnProperty(region)) {
    		var itemRows = sorted[region].map(function(item){
    			var boundClick = function(item){
    				this.viewDetail(item);
    			}.bind(this, item);
					return(
						<div key={item.id} className="truncate">
							<i className='fa fa-file-text btn btn-info btn-sm'
								onClick={boundClick} /> &ensp;
							<strong>{item.id}:</strong> {item.description}
						</div>
					)
				}.bind(this));
				rows.push(
					<div key={region} style={{marginBottom:"1rem"}}>
						<div>
							<strong>{region}</strong>
						</div>
						<div style={{paddingLeft:"1rem"}}> 
							{itemRows}
						</div>
					</div>
					)
    	}
    }
		var regionSelect = this.props.awsConfig.regions ?
			(
				<select ref="region" placeholder="Select one">
					{this.props.awsConfig.regions.map(function(region) {
						return(
							<option value={region[0]} key={region[0]}>{region[1]}</option>
						);
					})}
				</select>
			)
			:"";
		var foundRegions = null;
		if(this.state.modalState === 'searching') {
			foundRegions = (
				<div>
					<i className="fa fa-spinner fa-spin fa-2x"></i>
					Loading data from AWS
				</div>
				);
		} else {
			var currentIds = this.state.data.map(function(item){return item.id});
			foundRegions = this.state.regionImages.filter(function(image){
					return currentIds.indexOf(image.imageId)<0;
				}.bind(this)).map(function(image){
					var boundClick = function(image){
						this.addImage(image);
					}.bind(this, image);
					return(
						<div className="truncate" style={{padding:"2px"}} 
								onClick={boundClick} key={image.id}>
							<i className='fa fa-download btn btn-info btn-sm' /> &emsp;
							{image.imageId}: {image.name}
						</div>
					);
				}.bind(this));
		};
		var detail = this.state.detailItem ?
			(<ImageDetail image={this.state.detailItem} />)
			:
			(<div>Select an Item at left to view details</div>);
		
    var modal = (
      <BootstrapModal
        ref="modal"
        onCancel={this.onCancel}
        title="Lookup Images">
        <div style={{marginBottom: "20px"}}>
	        {regionSelect} &emsp;
	        <button className="btn btn-sm btn-primary" onClick={this.searchImages}>
	        	Search
	        </button>
	      </div>
	      <div>
	      	{foundRegions}
	      </div>
      </BootstrapModal>
    );
		return(
			<div>
				<div style={{marginBottom:"10px;"}}>
					<button className="btn btn-sm btn-primary" onClick={this.openModal}>
						Lookup Images
					</button>
				</div>
				{this.state.data.length == 0 ?
					<i className="fa fa-spinner fa-spin fa-2x"></i>
					:
					<div className="row">
						<div className="col-sm-7">
							{rows}
						</div>
						<div className="col-sm-5">
							{detail}
						</div>
					</div>
				}
				{modal}
			</div>
		);
	}

});