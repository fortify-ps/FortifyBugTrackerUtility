package com.fortify.processrunner.fod.processor;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

public class FoDFilterOnTopLevelFields extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FoDFilterOnTopLevelFields.class);
	private Map<String,String> filters;
	
	public FoDFilterOnTopLevelFields() {}
	
	public FoDFilterOnTopLevelFields(Map<String,String> filters) {
		this.filters = filters;
	}
	
	
	@Override
	protected boolean preProcess(Context context) {
		LOG.debug("Adding top-level field filters to request parameter value: "+getFilters()!=null);
		if ( getFilters() != null ) {
			for ( Map.Entry<String,String> filter : getFilters().entrySet() ) {
				appendTopLevelFieldFilterParamValue(context, filter.getKey(), filter.getValue());
			}
		}
		return true;
	}

	private void appendTopLevelFieldFilterParamValue(Context context, String key, String value) {
		IContextFoD contextFoD = context.as(IContextFoD.class);
		String currentParamValue = contextFoD.getFoDTopLevelFilterParamValue();
		if ( StringUtils.isBlank(currentParamValue) ) {
			currentParamValue = "";
		} else {
			currentParamValue += "+";
		}
		// TODO Use better URL encoding algorithm
		// TODO Filtering doesn't work if multiple filters are specified 
		contextFoD.setFoDTopLevelFilterParamValue(currentParamValue + key+":"+value.replace(" ", "%20"));
	}

	@Override
	// Since FoD doesn't support URL filtering on all top level fields (for example criticalityString),
	// we explicitly filter the results again.
	protected boolean process(Context context) {
		LOG.debug("Filtering top-level fields: "+getFilters()!=null);
		if ( getFilters() != null ) {
			JSONObject vuln = context.as(IContextFoD.class).getFoDCurrentVulnerability();
			for ( Map.Entry<String,String> filter : getFilters().entrySet() ) {
				String value = SpringExpressionUtil.evaluateExpression(vuln, filter.getKey(), String.class);
				if ( !filter.getValue().equals(value) ) { return false; }
			}
		}
		return true;
	}

	public Map<String, String> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, String> filters) {
		this.filters = filters;
	}
}
