package com.fortify.util.rest;

public interface IRestConnectionRetriever<C extends IRestConnection> {
	public C getConnection();
}
