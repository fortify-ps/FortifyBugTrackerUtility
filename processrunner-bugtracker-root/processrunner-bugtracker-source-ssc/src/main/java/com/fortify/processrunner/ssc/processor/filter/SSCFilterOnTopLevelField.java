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
package com.fortify.processrunner.ssc.processor.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.context.IContextSSCSource;

/**
 * <p>This {@link IProcessor} implementation allows for simple contains-based
 * filtering on top-level SSC vulnerability fields like severity and assignee.
 * The configured filter will be appended as a filter request parameter when 
 * requesting the list of vulnerabilities from SSC, allowing SSC to perform the 
 * filtering for optimal performance.</p>
 * 
 * <p>Depending on the excludeVulnerabilityWithMatchingValue flag, 
 * vulnerabilities for which a field matches the configured filter value will 
 * either be excluded from further processing (flag set to true) or included for
 * further processing (flag set to false, default).</p>
 * 
 * @author Ruud Senden
 */
public class SSCFilterOnTopLevelField extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(SSCFilterOnTopLevelField.class);
	private String fieldName;
	private String filterValue;
	private boolean excludeVulnerabilityWithMatchingValue = false;
	
	public SSCFilterOnTopLevelField() {}
	
	public SSCFilterOnTopLevelField(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public SSCFilterOnTopLevelField(String fieldName, String filterValue, boolean excludeVulnerabilityWithMatchingValue) {
		this.fieldName = fieldName;
		this.filterValue = filterValue;
		this.excludeVulnerabilityWithMatchingValue = excludeVulnerabilityWithMatchingValue;
	}
	
	
	@Override
	protected boolean preProcess(Context context) {
		LOG.debug("[SSC] Adding top-level field filter "+getFieldName()+getOperator()+getFilterValue());
		appendTopLevelFieldFilterParamValue(context, getFieldName(), getFilterValue());
		return true;
	}

	protected String getOperator() {
		return isExcludeVulnerabilityWithMatchingValue() ? ":!" : ":";
	}

	protected void appendTopLevelFieldFilterParamValue(Context context, String key, String value) {
		IContextSSCSource contextSSC = context.as(IContextSSCSource.class);
		String currentParamValue = contextSSC.getSSCTopLevelFilterParamValue();
		if ( StringUtils.isBlank(currentParamValue) ) {
			currentParamValue = "";
		} else {
			currentParamValue += "+";
		}
		try {
			contextSSC.setSSCTopLevelFilterParamValue(currentParamValue + key+getOperator()+URLEncoder.encode(value,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error encoding URL for filter values", e);
		}
	}

	@Override
	protected boolean process(Context context) {
		// We assume SSC has correctly filtered it's output, so we simply return true.
		return true;
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

	public boolean isExcludeVulnerabilityWithMatchingValue() {
		return excludeVulnerabilityWithMatchingValue;
	}

	public void setExcludeVulnerabilityWithMatchingValue(boolean excludeVulnerabilityWithMatchingValue) {
		this.excludeVulnerabilityWithMatchingValue = excludeVulnerabilityWithMatchingValue;
	}

	
}
