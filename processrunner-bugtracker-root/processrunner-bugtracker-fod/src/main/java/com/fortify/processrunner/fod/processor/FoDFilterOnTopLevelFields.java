package com.fortify.processrunner.fod.processor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * <p>This {@link IProcessor} implementation allows for simple equality-based
 * filtering on top-level FoD vulnerability fields like severity and assignee.
 * Any filters configured on this class will be appended as filter request
 * parameters when requesting the list of vulnerabilities from FoD, allowing
 * FoD to perform the filtering for optimal performance.</p>
 * 
 * <p>Since FoD currently ignores filters for some specific fields,
 * this processor will also manually filter each vulnerabilities based
 * on the configured filters.</p> 
 */
public class FoDFilterOnTopLevelFields extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FoDFilterOnTopLevelFields.class);
	private Map<String,String> filters;
	
	public FoDFilterOnTopLevelFields() {}
	
	public FoDFilterOnTopLevelFields(Map<String,String> filters) {
		this.filters = filters;
	}
	
	
	@Override
	protected boolean preProcess(Context context) {
		LOG.debug("Adding top-level field filters to request parameter value: "+(getFilters()!=null));
		if ( getFilters() != null ) {
			try {
				for ( Map.Entry<String,String> filter : getFilters().entrySet() ) {
					appendTopLevelFieldFilterParamValue(context, filter.getKey(), filter.getValue());
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return true;
	}

	private void appendTopLevelFieldFilterParamValue(Context context, String key, String value) throws UnsupportedEncodingException {
		IContextFoD contextFoD = context.as(IContextFoD.class);
		String currentParamValue = contextFoD.getFoDTopLevelFilterParamValue();
		if ( StringUtils.isBlank(currentParamValue) ) {
			currentParamValue = "";
		} else {
			currentParamValue += "%2B";
		}
		contextFoD.setFoDTopLevelFilterParamValue(currentParamValue + key+":"+URLEncoder.encode(value,"UTF-8"));
	}

	@Override
	// Since FoD doesn't support URL filtering on all top level fields (for example criticalityString),
	// we explicitly filter the results again.
	protected boolean process(Context context) {
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
