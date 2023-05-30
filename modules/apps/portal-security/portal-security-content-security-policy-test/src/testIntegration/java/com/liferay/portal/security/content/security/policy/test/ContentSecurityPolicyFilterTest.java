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

package com.liferay.portal.security.content.security.policy.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.settings.SettingsFactoryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Olivér Kecskeméty
 */
@FeatureFlags("LPS-134060")
@RunWith(Arquillian.class)
public class ContentSecurityPolicyFilterTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testProcessFilter() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				configurationTemporarySwapper = _configureContentSecurityPolicy(
					false, "")) {

			HttpURLConnection httpURLConnection = _openHttpURLConnection();

			Map<String, List<String>> headerFields =
				httpURLConnection.getHeaderFields();

			Assert.assertFalse(
				headerFields.containsKey("Content-Security-Policy"));
		}

		try (CompanyConfigurationTemporarySwapper
				configurationTemporarySwapper = _configureContentSecurityPolicy(
					true, "")) {

			HttpURLConnection httpURLConnection = _openHttpURLConnection();

			Map<String, List<String>> headerFields =
				httpURLConnection.getHeaderFields();

			Assert.assertFalse(
				headerFields.containsKey("Content-Security-Policy"));

			String content = _getContent(httpURLConnection);

			Assert.assertFalse(content.contains("nonce="));
		}

		String policy = "default-src 'self';";

		try (CompanyConfigurationTemporarySwapper
				configurationTemporarySwapper = _configureContentSecurityPolicy(
					true, policy)) {

			HttpURLConnection httpURLConnection = _openHttpURLConnection();

			Map<String, List<String>> headerFields =
				httpURLConnection.getHeaderFields();

			Assert.assertTrue(
				headerFields.containsKey("Content-Security-Policy"));

			Assert.assertEquals(
				httpURLConnection.getHeaderField("Content-Security-Policy"),
				policy);
		}

		policy =
			"default-src 'self'; script-src 'self' '[$NONCE$]'; style-src " +
				"'self' '[$NONCE$]'";

		try (CompanyConfigurationTemporarySwapper
				configurationTemporarySwapper = _configureContentSecurityPolicy(
					true, policy)) {

			HttpURLConnection httpURLConnection = _openHttpURLConnection();

			Map<String, List<String>> headerFields =
				httpURLConnection.getHeaderFields();

			Assert.assertTrue(
				headerFields.containsKey("Content-Security-Policy"));

			String value = httpURLConnection.getHeaderField(
				"Content-Security-Policy");

			Assert.assertNotNull(value);

			int index = value.indexOf("nonce-") + "nonce-".length();

			String nonce = value.substring(index, index + 24);

			Assert.assertEquals(
				value,
				StringUtil.replace(policy, "[$NONCE$]", "nonce-" + nonce));

			String content = _getContent(httpURLConnection);

			Assert.assertTrue(
				content.contains("<link nonce=\"" + nonce + "\""));
			Assert.assertTrue(
				content.contains("<script nonce=\"" + nonce + "\""));
			Assert.assertTrue(
				content.contains("<style nonce=\"" + nonce + "\""));
		}
	}

	private CompanyConfigurationTemporarySwapper
			_configureContentSecurityPolicy(boolean enabled, String policy)
		throws Exception {

		return new CompanyConfigurationTemporarySwapper(
			TestPropsValues.getCompanyId(),
			"com.liferay.portal.security.content.security.policy.internal." +
				"configuration.ContentSecurityPolicyConfiguration",
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", enabled
			).put(
				"policy", policy
			).build(),
			SettingsFactoryUtil.getSettingsFactory());
	}

	private HttpURLConnection _openHttpURLConnection() throws IOException {
		URL url = new URL("http://localhost:8080/web/guest");

		HttpURLConnection httpURLConnection =
			(HttpURLConnection)url.openConnection();

		httpURLConnection.setRequestMethod("GET");

		return httpURLConnection;
	}

	private String _getContent(HttpURLConnection httpURLConnection)
		throws IOException {

		StringBuilder sb = new StringBuilder();

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(httpURLConnection.getInputStream()))) {

			String line;

			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
		}

		return sb.toString();
	}

}