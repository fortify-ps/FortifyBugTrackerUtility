package com.fortify.processrunner.common;

/**
 * This class holds information about issues that have been
 * submitted to an external system. Bug tracker implementations
 * can optionally use subclasses to hold additional information.
 */
public class SubmittedIssue {
	private String id;
	private String deepLink;
	
	public SubmittedIssue() {}
	
	public SubmittedIssue(String id, String deepLink) {
		this.id = id;
		this.deepLink = deepLink;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the deepLink
	 */
	public String getDeepLink() {
		return deepLink;
	}
	/**
	 * @param deepLink the deepLink to set
	 */
	public void setDeepLink(String deepLink) {
		this.deepLink = deepLink;
	}
	
	
}
