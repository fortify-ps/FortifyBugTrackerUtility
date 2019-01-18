/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.bugtracker.src.ssc.config;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fortify.bugtracker.common.src.config.AbstractSourceVulnerabilitiesConfiguration;
import com.fortify.bugtracker.common.ssc.json.preprocessor.filter.SSCJSONMapFilterWithLoggerApplicationVersionHasAllCustomTags;
import com.fortify.bugtracker.common.ssc.json.preprocessor.filter.SSCJSONMapFilterWithLoggerApplicationVersionHasBugTrackerShortDisplayName;
import com.fortify.bugtracker.common.ssc.query.ISSCApplicationVersionQueryBuilderUpdater;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionsQueryBuilder;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This class holds all SSC-related configuration properties used to submit vulnerabilities
 * to a bug tracker or other external system, and performing issue state management.
 * 
 * @author Ruud Senden
 *
 */
public class SSCSourceVulnerabilitiesConfiguration extends AbstractSourceVulnerabilitiesConfiguration implements ISSCApplicationVersionQueryBuilderUpdater {
	static final SimpleExpression DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION = 
			SpringExpressionUtil.parseSimpleExpression("removed==false && suppressed==false");
	
	private String filterStringForVulnerabilitiesToBeSubmitted = null;
	private String bugLinkCustomTagName = null;
	private boolean addNativeBugLink = false;
	private String addNativeBugLinkBugTrackerName = "Add Existing Bugs";
	private Map<String,TemplateExpression> extraCustomTags = null;
	private boolean enableRevisionWorkAround = false;
	private String filterSetId = null;
	
	@Override
	public void updateQueryBuilder(Context context, SSCApplicationVersionsQueryBuilder builder) {
		if ( StringUtils.isNotBlank(getBugLinkCustomTagName()) ) {
			builder.preProcessor(new SSCJSONMapFilterWithLoggerApplicationVersionHasAllCustomTags(MatchMode.INCLUDE, getBugLinkCustomTagName()));
		} else if ( isAddNativeBugLink() ) {
			builder.preProcessor(new SSCJSONMapFilterWithLoggerApplicationVersionHasBugTrackerShortDisplayName(MatchMode.INCLUDE, getAddNativeBugLinkBugTrackerName()));
		}
	}
	
	@Override
	protected SimpleExpression getDefaultIsVulnerabilityOpenExpression() {
		return DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION;
	}
	
	public String getFilterStringForVulnerabilitiesToBeSubmitted() {
		return filterStringForVulnerabilitiesToBeSubmitted;
	}

	public void setFilterStringForVulnerabilitiesToBeSubmitted(String sscFilterStringForVulnerabilitiesToBeSubmitted) {
		this.filterStringForVulnerabilitiesToBeSubmitted = sscFilterStringForVulnerabilitiesToBeSubmitted;
	}

	public String getBugLinkCustomTagName() {
		return bugLinkCustomTagName;
	}

	public void setBugLinkCustomTagName(String bugLinkCustomTagName) {
		this.bugLinkCustomTagName = bugLinkCustomTagName;
	}

	public boolean isAddNativeBugLink() {
		return addNativeBugLink;
	}

	public void setAddNativeBugLink(boolean addNativeBugLink) {
		this.addNativeBugLink = addNativeBugLink;
	}

	public String getAddNativeBugLinkBugTrackerName() {
		return addNativeBugLinkBugTrackerName;
	}

	public void setAddNativeBugLinkBugTrackerName(String addNativeBugLinkBugTrackerName) {
		this.addNativeBugLinkBugTrackerName = addNativeBugLinkBugTrackerName;
	}

	public Map<String, TemplateExpression> getExtraCustomTags() {
		return extraCustomTags;
	}

	public void setExtraCustomTags(Map<String, TemplateExpression> extraCustomTags) {
		this.extraCustomTags = extraCustomTags;
	}

	public boolean isEnableRevisionWorkAround() {
		return enableRevisionWorkAround;
	}

	public void setEnableRevisionWorkAround(boolean enableRevisionWorkAround) {
		this.enableRevisionWorkAround = enableRevisionWorkAround;
	}

	public String getFilterSetId() {
		return filterSetId;
	}

	public void setFilterSetId(String filterSetId) {
		this.filterSetId = filterSetId;
	}
	
}
