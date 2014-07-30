
package com.liferay.test.portal.jsp;

public class PortalMasterProjectLocation {

	public static String getDocroot() {
		return getLocation() + "/portal-web/docroot";
	}

	public static String getLocation() {
		String env = System.getenv(ENV_VAR_NAME);

		if (env != null)
			return env;

		throw new RuntimeException(
			"Env var " + ENV_VAR_NAME + " not set." +
				" You should use the provided launch configuration.");
	}

	private static final String ENV_VAR_NAME = "PORTAL_MASTER_PROJECT_LOCATION";

}
