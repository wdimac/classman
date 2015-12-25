/**
 * Copyright (C) 2016 AppDynamics
 */

// Store auth token locally
var Auth = function() {
	var authenticateUser = function(username, password, callback) {
	  $.ajax({
	  		url:'/api/authenticate',
	    	type: 'POST',
  	    data: {username: username, password: password},
  	    success: function(resp) {
  	          callback( {authenticated:true, token:resp.auth_token});},
  	    error: function(resp) {
  	          callback({authenticated:false});}
  	});
  };

	var loggedIn = function(){
		return !!localStorage.token;
	};

	var logIn = function(username, password, callback) {
		authenticateUser (username, password, function(res) {
      var authenticated = false
      if (res.authenticated){
        localStorage.token = res.token;
        authenticated = true;
      }
      if (callback) callback(authenticated);
    });

	};

	var logOut = function() {
		delete localStorage.token;
		this.onChange(false);
	};

	var getToken = function() {
		return localStorage.token;
	}

	return {
		logIn:logIn,
		loggedIn: loggedIn,
		logOut: logOut,
		getToken: getToken,
		onChange: function(authenticated) {}
	}
}();