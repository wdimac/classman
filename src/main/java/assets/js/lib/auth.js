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
		if (this.localAvailable) {
			return !!localStorage.token;
		} else {
			return !!this.token;
		}
	};

	var logIn = function(username, password, callback) {
		authenticateUser (username, password, function(res) {
      var authenticated = false
      if (res.authenticated){
      	if (this.localAvailable) {
	        localStorage.token = res.token;
        } else {
        	this.token = res.token;
        }
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
		if (this.localAvailable) {
			return localStorage.token;
		} else {
			return this.token;
		}
	}

	var localAvailable = function() {
		try {
	    var x = 'test-localstorage-' + Date.now();
	    localStorage.setItem(x, x);
	    var y = localStorage.getItem(x);
	    localStorage.removeItem(x);
	    if (y !== x) {throw new Error();}
	    return true; // localStorage is fully functional!
		} catch (e) {
			return false;
		}
	}
	return {
		logIn:logIn,
		loggedIn: loggedIn,
		logOut: logOut,
		getToken: getToken,
		onChange: function(authenticated) {},
		localAvailable: localAvailable()
	}
}();