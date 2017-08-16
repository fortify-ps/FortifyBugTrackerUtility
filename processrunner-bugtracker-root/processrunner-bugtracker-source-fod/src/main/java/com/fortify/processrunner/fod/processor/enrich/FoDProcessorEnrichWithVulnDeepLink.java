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
package com.fortify.processrunner.fod.processor.enrich;

import com.fortify.processrunner.common.processor.enrich.AbstractProcessorEnrichCurrentVulnerabilityWithDeepLink;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link AbstractProcessorEnrichCurrentVulnerabilityWithDeepLink} provides an FoD-specific 
 * deep link URI expression.
 * 
 * @author Ruud Senden
 */
public class FoDProcessorEnrichWithVulnDeepLink extends AbstractProcessorEnrichCurrentVulnerabilityWithDeepLink {
	private static final TemplateExpression DEEP_LINK_URI_EXPRESSION = SpringExpressionUtil.parseTemplateExpression("redirect/Issues/${vulnId}");
	
	@Override
	protected TemplateExpression getDeepLinkUriExpression() {
		return DEEP_LINK_URI_EXPRESSION;
	}
	
	@Override
	protected IRestConnection getConnection(Context context) {
		return FoDConnectionFactory.getConnection(context);
	}
	
}
