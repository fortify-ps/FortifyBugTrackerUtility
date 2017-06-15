package com.fortify.processrunner.fod.processor.enrich;

import com.fortify.processrunner.common.context.IContextCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.util.json.JSONMap;

public abstract class AbstractFoDProcessorEnrich extends AbstractProcessor {
	@Override
	protected boolean process(Context context) {
		JSONMap currentVulnerability = (JSONMap) context.as(IContextCurrentVulnerability.class).getCurrentVulnerability();
		return enrich(context, currentVulnerability);
	}

	protected abstract boolean enrich(Context context, JSONMap currentVulnerability);
}
