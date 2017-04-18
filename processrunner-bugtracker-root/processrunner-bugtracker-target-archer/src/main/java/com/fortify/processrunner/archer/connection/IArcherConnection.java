package com.fortify.processrunner.archer.connection;

import java.util.LinkedHashMap;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.rest.IRestConnection;

public interface IArcherConnection extends IRestConnection {
	Long addValueToValuesList(Long valueListId, String value);
	SubmittedIssue submitIssue(LinkedHashMap<String, Object> issueData);
}