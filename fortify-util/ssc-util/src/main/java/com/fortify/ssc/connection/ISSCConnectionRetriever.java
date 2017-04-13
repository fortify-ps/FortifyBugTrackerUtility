package com.fortify.ssc.connection;

import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * Marker interface that extends {@link IRestConnectionRetriever}
 * to differentiate from other (non-SSC) connection factories,
 * for example when using auto-wiring.
 */
public interface ISSCConnectionRetriever extends IRestConnectionRetriever<SSCAuthenticatingRestConnection> {
	public abstract String getBaseUrl();
}
