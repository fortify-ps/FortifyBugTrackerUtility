package com.fortify.processrunner.archer.connection;

import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * Marker interface that extends {@link IRestConnectionRetriever}
 * to differentiate from other (non-Archer) connection factories,
 * for example when using auto-wiring.
 */
public interface IArcherConnectionRetriever extends IRestConnectionRetriever<ArcherAuthenticatingRestConnection> {
	public abstract String getBaseUrl();
}
