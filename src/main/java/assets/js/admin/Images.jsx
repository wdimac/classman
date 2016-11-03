var Accordian = window.__APP__.Accordian;
var BootstrapModal = window.__APP__.BootstrapModal;
var Launcher = window.__APP__.Launcher;
var PropsSpewer = window.__APP__.PropsSpewer;

//Display of image detail
var ImageDetail = React.createClass({
	getInitialState() {
		return {
			detailImage:null
		}
	},
	loadDataFromServer() {
		var url = "/api/admin/aws/"
			+ this.props.image.region + '/images/' + this.props.image.id;
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
		return (
			<div className="card">
				<div className="card-header bg-info">
					Image: {this.props.image.id}
					<a href="javascript:void(0);" title="Delete this image"
						onClick={this.props.handleDelete}>
						<i className="fa fa-lg fa-minus-square text-danger pull-right"></i>
					</a>
				</div>
				<div className="card-body p-a-1">
					<div><strong>AMI Name: </strong> {this.props.image.description}</div>
					{this.state.detailImage ?
						<div>
							<PropsSpewer 
                  item={this.state.detailImage}
                  omit={["imageId", "name"]}/>
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

var ImageLookup = React.createClass({
	getInitialState() {
		return {
			searchState:"waiting",
			regionImages: [],
			currentModalRegion:""
		}
	},
	open() {
		this.refs.modal.open();
	},
	close() {
		this.refs.modal.close();
	},
  searchImages() {
  	// redo search if we have switched regions
  	if (this.refs.region.value !== this.state.currentModalRegion) {
	  	this.setState({searchState:"searching"});
	  	$.ajax({
	      url: "/api/admin/aws/" + this.refs.region.value + "/images",
	      headers: {'X-AUTH-TOKEN':Auth.getToken()},
	      dataType: 'json',
	      cache: false,
	      success: function(data) {
	      	this.setState({
	      		regionImages: data, 
	      		searchState:"waiting", 
	      	});
	      }.bind(this),
	      error: function(xhr, status, err) {
	        console.error(this.props.url, status, err.toString());
	      }.bind(this)
	    });
	  }
  },
  addImage(image) {
  	$.ajax({
  		url: "/api/admin/images",
  		type: "POST",
      headers: {
        'X-AUTH-TOKEN':Auth.getToken()
       },
      contentType: 'application/json',
      dataType: 'json',
      data:JSON.stringify({id: image.imageId, description: image.name, region: this.refs.region.value}),
      cache: false,
      success: function(data) {
      	this.props.updateParent();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
  	});
  },
	render() {
		// build list of regions from AWS
		var foundRegions = null;
		if(this.state.searchState === 'searching') {
			foundRegions = (
				<div>
					<i className="fa fa-spinner fa-spin fa-2x fa-pull-left"></i>
					Loading data from AWS
				</div>
				);
		} else {
			var currentIds = this.props.existing.map(function(item){return item.id});
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
		// Build the select
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

		//FInal composition
		return (
			<BootstrapModal
        ref="modal"
        onCancel={this.close}
        title="Lookup Images">
        <div className="m-b-1">
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
	}
});

// Main images panel
window.__APP__.Images = React.createClass({
	getInitialState() {
		return {
			data:[],
			detailItem:null,
			launchItem:{},
      groups:[]
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
    $.ajax({
      url: "/api/admin/security_groups",
      headers: {'X-AUTH-TOKEN':Auth.getToken()},
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({groups: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  componentDidMount() {
    this.loadDataFromServer();
  },
  viewDetail(item) {
  	this.setState({detailItem:item});
  },
  openLookup() {
  	this.refs.lookup.open();
  },
  launch(item) {
  	this.setState({
  		launchItem: item
  	});
  	this.refs.launcher.open();
  },
  //Callback for Accordian
  formatImageRow(item) {
		var boundClick = function(item){
			this.viewDetail(item);
		}.bind(this, item);
		var launchClick = function(item){
			this.launch(item);
		}.bind(this, item);
		var defunct = item.defunct;
		if (defunct) {
		  return(
		    <div key={item.id} className="truncate">
		      <i className='fa fa-file-text btn btn-info btn-sm'
                onClick={boundClick} /> &ensp;Defunct - 
              <strong className="strike">{item.id}:</strong> {item.description}
            </div>
		  );
		} else {
		  return(
			<div key={item.id} className="truncate">
				<i className='fa fa-file-text btn btn-info btn-sm'
					onClick={boundClick} /> &ensp;
				<i className='fa fa-rocket btn btn-success btn-sm'
					onClick={launchClick} /> &ensp;
				<strong>{item.id}:</strong> {item.description}
			</div>
		  );
		}
	},
	//Callback for detail panel
	deleteDetailItem() {
		if (this.state.detailItem) {
			var url = '/api/admin/images/' + this.state.detailItem.id;
			$.ajax({
	      url: url,
	      headers: {'X-AUTH-TOKEN':Auth.getToken()},
	      type: 'DELETE',
	      dataType: 'json',
	      cache: false,
	      success: function(data) {
	      	this.setState({detailItem: null});
	      	this.loadDataFromServer();
	      }.bind(this),
	      error: function(xhr, status, err) {
	        console.error("DELETE " + url, status, err.toString());
	      }.bind(this)
	    });
		}
	},
	render() {
		var cRegion = "";
    var sorted = window.__APP__.sortByRegion(this.state.data);
		//Create detail panel
		var detail = this.state.detailItem ?
			(<ImageDetail image={this.state.detailItem} handleDelete={this.deleteDetailItem}/>)
			:
			(<div>Select an Item at left to view details</div>);
		return(
			<div>
				<div className="m-b-1">
					<button className="btn btn-sm btn-primary" onClick={this.openLookup}>
						Lookup Images
					</button>
				</div>
				{this.state.data.length == 0 ?
					<i className="fa fa-spinner fa-spin fa-2x"></i>
					:
					<div className="row">
						<div className="col-sm-7">
						<Accordian id="region_list" 
							formatItemRow={this.formatImageRow}
							map={sorted} />
						</div>
						<div className="col-sm-5">
							{detail}
						</div>
					</div>
				}
      	<ImageLookup ref="lookup" 
      		existing={this.state.data}
      		awsConfig={this.props.awsConfig}
      		updateParent={this.loadDataFromServer} />
      	<Launcher ref="launcher" 
	      	awsConfig={this.props.awsConfig}
          groups={this.state.groups}
      		target={this.state.launchItem} />
			</div>
		);
	}

});