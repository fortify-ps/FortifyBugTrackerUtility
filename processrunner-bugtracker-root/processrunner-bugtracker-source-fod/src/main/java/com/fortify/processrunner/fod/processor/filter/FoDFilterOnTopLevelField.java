/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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
package com.fortify.processrunner.fod.processor.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.context.IContextCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.json.JSONMap;

/**
 * <p>This {@link IProcessor} implementation allows for simple equality-based
 * filtering on top-level FoD vulnerability fields like severity and assignee.
 * The configured filter will be appended as a filter request parameter when 
 * requesting the list of vulnerabilities from FoD, allowing FoD to perform the 
 * filtering for optimal performance.</p>
 * 
 * <p>Since FoD currently ignores filters for some specific fields, this processor 
 * will also manually filter each vulnerabilities based on the configured filter. 
 * Filter values may use the FoD '|' (OR) operator to filter on multiple values for 
 * a given field.</p>
 * 
 * <p>Only vulnerabilities matching the configured filtering criteria will be 
 * processed; vulnerabilities not matching the criteria will not be processed 
 * any further.</p>
 */
public class FoDFilterOnTopLevelField extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FoDFilterOnTopLevelField.class);
	private String fieldName;
	private String filterValue;
	
	public FoDFilterOnTopLevelField() {}
	
	public FoDFilterOnTopLevelField(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public FoDFilterOnTopLevelField(String fieldName, String filterValue) {
		this.fieldName = fieldName;
		this.filterValue = filterValue;
	}
	
	
	@Override
	protected boolean preProcess(Context context) {
		LOG.debug("[FoD] Adding top-level field filter "+getFieldName()+"="+getFilterValue());
		appendTopLevelFieldFilterParamValue(context, getFieldName(), getFilterValue());
		return true;
	}

	protected void appendTopLevelFieldFilterParamValue(Context context, String key, String value) {
		IContextFoD contextFoD = context.as(IContextFoD.class);
		String currentParamValue = contextFoD.getFoDTopLevelFilterParamValue();
		if ( StringUtils.isBlank(currentParamValue) ) {
			currentParamValue = "";
		} else {
			currentParamValue += "%2B";
		}
		try {
			contextFoD.setFoDTopLevelFilterParamValue(currentParamValue + key+"%3A"+URLEncoder.encode(value,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error encoding URL for filter values", e);
		}
	}

	@Override
	// Since FoD doesn't support URL filtering on all top level fields (for example criticalityString),
	// we explicitly filter the results again.
	protected boolean process(Context context) {
		JSONMap vuln = (JSONMap) context.as(IContextCurrentVulnerability.class).getCurrentVulnerability();
		String value = ContextSpringExpressionUtil.evaluateExpression(context, vuln, getFieldName(), String.class);
		List<String> allowedValues = Arrays.asList(getFilterValue().split("\\|"));
		return allowedValues.contains(value);
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return the filterValue
	 */
	public String getFilterValue() {
		return filterValue;
	}

	/**
	 * @param filterValue the filterValue to set
	 */
	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	
}
