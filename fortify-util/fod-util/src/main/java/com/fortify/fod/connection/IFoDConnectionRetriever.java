package com.fortify.fod.connection;

import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * Marker interface that extends {@link IRestConnectionRetriever}
 * to differentiate from other (non-FoD) connection factories,
 * for example when using auto-wiring.
 */
public interface IFoDConnectionRetriever extends IRestConnectionRetriever {
	public abstract String getBaseUrl();
}
