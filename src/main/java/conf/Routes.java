/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */

package conf;

import com.google.inject.Inject;

import controllers.ApplicationController;
import controllers.DbController;
import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import ninja.jaxy.JaxyRoutes;
import ninja.utils.NinjaProperties;

public class Routes implements ApplicationRoutes {
	@Inject
	JaxyRoutes jaxyRoutes;

	@Inject
	NinjaProperties ninjaProperties;

	@Override
	public void init(Router router) {
		///////////////////////////////////////////////////////////////////////
		// Find annotated routes
		///////////////////////////////////////////////////////////////////////
		jaxyRoutes.init(router);

		///////////////////////////////////////////////////////////////////////
		// Test/Dev only controller
		///////////////////////////////////////////////////////////////////////
		if (!ninjaProperties.isProd()) {
			router.GET().route("/db/reset").with(DbController.class, "reset");
		}

		///////////////////////////////////////////////////////////////////////
		// Assets (pictures / javascript)
		///////////////////////////////////////////////////////////////////////
		router.GET().route("/assets/webjars/{fileName: .*}").with(AssetsController.class, "serveWebJars");
		router.GET().route("/assets/{fileName: .*}").with(AssetsController.class, "serveStatic");

		///////////////////////////////////////////////////////////////////////
		// Index / Catchall shows index page
		///////////////////////////////////////////////////////////////////////
		router.GET().route("/.*").with(ApplicationController.class, "index");

	}

}
