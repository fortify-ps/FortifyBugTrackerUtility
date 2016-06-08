package com.fortify.processrunner.jira.connection;

import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * Marker interface that extends {@link IRestConnectionRetriever}
 * to differentiate from other (non-Jira) connection factories,
 * for example when using auto-wiring.
 */
public interface IJiraConnectionRetriever extends IRestConnectionRetriever {

}
