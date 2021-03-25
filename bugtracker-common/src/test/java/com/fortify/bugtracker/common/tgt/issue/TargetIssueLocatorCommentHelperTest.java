/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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
package com.fortify.bugtracker.common.tgt.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fortify.util.spring.expression.helper.DefaultExpressionHelper;

public class TargetIssueLocatorCommentHelperTest {

	@Test
	public void testFromTargetName() {
		String id = "someId";
		String deepLink = "http://somehost:port/path?q1=abc&q2=def#xyz";
		testCommentHelper(TargetIssueLocatorCommentHelper.fromTargetName("test (abc)"),
			id, id, deepLink, deepLink);
	}
	
	@Test
	public void testFromTemplateExpressionWithIdAndDeepLink() {
		String id = "someId";
		String deepLink = "http://somehost:port/path?q1=abc&q2=def#xyz";
		testCommentHelper(TargetIssueLocatorCommentHelper.fromTemplateExpression(
				DefaultExpressionHelper.get().parseTemplateExpression("BlaBla ${id} -- ${deepLink}")),
			id, id, deepLink, deepLink);
	}
	
	@Test
	public void testFromTemplateExpressionWithoutId() {
		String id = "someId";
		String deepLink = "http://somehost:port/path?q1=abc&q2=def#xyz";
		testCommentHelper(TargetIssueLocatorCommentHelper.fromTemplateExpression(
				DefaultExpressionHelper.get().parseTemplateExpression("BlaBla ${deepLink}")),
			id, null, deepLink, deepLink);
	}
	
	@Test
	public void testFromTemplateExpressionWithoutDeepLink() {
		String id = "someId";
		String deepLink = "http://somehost:port/path?q1=abc&q2=def#xyz";
		testCommentHelper(TargetIssueLocatorCommentHelper.fromTemplateExpression(
				DefaultExpressionHelper.get().parseTemplateExpression("BlaBla ${id}")),
			id, id, deepLink, null);
	}
	
	@Test
	public void testFromTemplateExpressionWithSpecialChars() {
		String id = "someId";
		String deepLink = "http://somehost:port/path?q1=abc&q2=def#xyz";
		testCommentHelper(TargetIssueLocatorCommentHelper.fromTemplateExpression(
				DefaultExpressionHelper.get().parseTemplateExpression("BlaBla $ { ' [ ] ' } '${id}' -- '${deepLink}'")),
			id, id, deepLink, deepLink);
	}

	private void testCommentHelper(TargetIssueLocatorCommentHelper helper, String orgId, String expectedId, String orgDeepLink, String expectedDeepLink) {
		System.out.println("Template Expression: "+helper.getTemplateExpression().getExpressionString());
		System.out.println("Match Pattern:       "+helper.getMatchPattern().pattern());
		System.out.println("Parser Pattern:      "+helper.getParser().toPattern());
		
		
		String comment = helper.getCommentForSubmittedIssue(new TargetIssueLocator(orgId, orgDeepLink));
		System.out.println("Comment: "+comment);
		assertTrue("Generated pattern does not match generated comment", helper.getMatchPattern().matcher(comment).matches());
		TargetIssueLocator parsedLocator = helper.getTargetIssueLocatorFromComment(comment);
		assertEquals("Parsed id doesn't match original id", expectedId, parsedLocator.getId());
		assertEquals("Parsed deepLink doesn't match original deepLink", expectedDeepLink, parsedLocator.getDeepLink());
	}

}
