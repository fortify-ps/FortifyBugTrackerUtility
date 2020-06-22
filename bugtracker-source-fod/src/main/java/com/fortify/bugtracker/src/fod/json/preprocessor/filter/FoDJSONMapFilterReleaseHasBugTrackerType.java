/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.bugtracker.src.fod.json.preprocessor.filter;

import java.util.Set;

import com.fortify.client.fod.api.query.builder.FoDReleasesQueryBuilder;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter;
import com.fortify.util.rest.query.IRestConnectionQueryConfigAware;

/**
 * Filter FoD releases based on the FoD bug tracker plugin type id configured
 * for each application.
 * 
 * @author Ruud Senden
 *
 */
public class FoDJSONMapFilterReleaseHasBugTrackerType extends AbstractJSONMapFilter implements IRestConnectionQueryConfigAware<FoDReleasesQueryBuilder>{
	private static final Integer TYPE_ID_OTHER = new Integer(2);
	private final Set<String> allowedBugTrackerTypes;
	
	public FoDJSONMapFilterReleaseHasBugTrackerType(MatchMode matchMode, Set<String> allowedBugTrackerTypes) {
		super(matchMode);
		this.allowedBugTrackerTypes = allowedBugTrackerTypes;
	}
	
	@Override
	protected boolean isMatching(JSONMap json) {
		return isMatchingNone(json) || isMatchingOther(json) || isMatchingType(json);
	}

	private boolean isMatchingType(JSONMap json) {
		return true == json.getPath("application.bugTracker.enabled", Boolean.class) 
			 && allowedBugTrackerTypes.contains(json.getPath("application.bugTracker.bugTrackerType", String.class));
	}

	private boolean isMatchingNone(JSONMap json) {
		return false == json.getPath("application.bugTracker.enabled", Boolean.class) 
				 && allowedBugTrackerTypes.contains("None");
	}
	
	private boolean isMatchingOther(JSONMap json) {
		return true == json.getPath("application.bugTracker.enabled", Boolean.class) 
				// For 'Other' bug tracker, type is null so we check type id to be certain
				 && TYPE_ID_OTHER.equals(json.getPath("application.bugTracker.bugTrackerTypeId", Integer.class))
				 && allowedBugTrackerTypes.contains("Other");
	}
	
	@Override
	public void setRestConnectionQueryConfig(FoDReleasesQueryBuilder builder) {
		builder.onDemandApplication();
	}
}
