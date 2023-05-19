/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.vulcan.internal.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PropsValues;
import com.liferay.portal.vulcan.internal.test.util.URLConnectionUtil;

import java.net.URLConnection;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matija Petanjek
 */
@RunWith(Arquillian.class)
public class ReverseProxyTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testActionsHrefValues() throws Exception {
		URLConnection urlConnection = URLConnectionUtil.createURLConnection(
			String.format(
				"http://localhost:8080/o/headless-delivery/v1.0/sites/%s" +
					"/blog-postings",
				TestPropsValues.getGroupId()));

		boolean webServerForwardedHostEnabled =
			PropsValues.WEB_SERVER_FORWARDED_HOST_ENABLED;
		boolean webServerForwardedProtocolEnabled =
			PropsValues.WEB_SERVER_FORWARDED_PROTOCOL_ENABLED;

		try {
			ReflectionTestUtil.setFieldValue(
				PropsValues.class, "WEB_SERVER_FORWARDED_HOST_ENABLED", true);
			ReflectionTestUtil.setFieldValue(
				PropsValues.class, "WEB_SERVER_FORWARDED_PROTOCOL_ENABLED",
				true);

			urlConnection.addRequestProperty("X-Forwarded-Host", "myHost");
			urlConnection.addRequestProperty("X-Forwarded-Proto", "https");

			String href = _getHref(urlConnection);

			Assert.assertTrue(href.startsWith("https://myHost:8080"));
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				PropsValues.class, "WEB_SERVER_FORWARDED_HOST_ENABLED",
				webServerForwardedHostEnabled);
			ReflectionTestUtil.setFieldValue(
				PropsValues.class, "WEB_SERVER_FORWARDED_PROTOCOL_ENABLED",
				webServerForwardedProtocolEnabled);
		}
	}

	private String _getHref(URLConnection urlConnection) throws Exception {
		urlConnection.connect();

		JsonNode jsonNode = _objectMapper.readTree(
			urlConnection.getInputStream());

		jsonNode = jsonNode.at("/actions/create/href");

		return jsonNode.asText();
	}

	private final ObjectMapper _objectMapper = new ObjectMapper();

}