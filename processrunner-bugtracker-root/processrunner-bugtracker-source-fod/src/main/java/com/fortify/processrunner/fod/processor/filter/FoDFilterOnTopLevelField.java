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
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;

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
		String value = SpringExpressionUtil.evaluateExpression(vuln, getFieldName(), String.class);
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
