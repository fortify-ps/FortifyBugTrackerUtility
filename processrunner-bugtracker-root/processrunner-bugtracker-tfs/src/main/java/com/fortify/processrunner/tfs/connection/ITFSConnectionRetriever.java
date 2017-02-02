package com.fortify.processrunner.tfs.connection;

import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * Marker interface that extends {@link IRestConnectionRetriever}
 * to differentiate from other (non-TFS) connection factories,
 * for example when using auto-wiring.
 */
public interface ITFSConnectionRetriever extends IRestConnectionRetriever<TFSRestConnection> {

}
