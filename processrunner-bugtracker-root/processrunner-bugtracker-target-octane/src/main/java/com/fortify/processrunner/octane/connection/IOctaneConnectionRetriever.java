package com.fortify.processrunner.octane.connection;

import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * Marker interface that extends {@link IRestConnectionRetriever}
 * to differentiate from other (non-Octane) connection factories,
 * for example when using auto-wiring.
 */
public interface IOctaneConnectionRetriever extends IRestConnectionRetriever<OctaneAuthenticatingRestConnection> {
	public abstract String getBaseUrl();
}
