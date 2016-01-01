/**
 * Copyright (C) 2016 AppDynamics
 */

// Store auth token locally
var Auth = {
	authenticateUser: function(username, password, callback) {
	  $.ajax({
	  		url:'/api/authenticate',
	    	type: 'POST',
  	    data: {username: username, password: password},
  	    success: function(resp) {
  	          callback( {authenticated:true, token:resp.auth_token});},
  	    error: function(resp) {
  	          callback({authenticated:false});}
  	});
  },

	loggedIn: function(){
		if (Auth.localAvailable) {
			return !!localStorage.token;
		} else {
			return !!Auth.token;
		}
	},

	logIn: function(username, password, callback) {
		Auth.authenticateUser (username, password, function(res) {
      var authenticated = false
      if (res.authenticated){
      	if (Auth.localAvailable) {
	        localStorage.token = res.token;
        } else {
        	Auth.token = res.token;
        }
      	authenticated = true;
      }
      if (callback) callback(authenticated);
    });

	},

	logOut: function() {
		delete localStorage.token;
		Auth.onChange(false);
	},

	getToken: function() {
		if (Auth.localAvailable) {
			return localStorage.token;
		} else {
			return Auth.token;
		}
	},

  onChange: function(authenticated) {},

	localAvailable: function() {
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
	}()
};