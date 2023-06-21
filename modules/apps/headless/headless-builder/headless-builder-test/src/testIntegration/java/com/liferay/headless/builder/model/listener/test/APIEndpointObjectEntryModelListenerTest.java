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

package com.liferay.headless.builder.model.listener.test;

import com.liferay.headless.builder.test.BaseTestCase;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.test.rule.FeatureFlags;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sergio Jiménez del Coso
 */
@FeatureFlags({"LPS-153117", "LPS-167253", "LPS-184413"})
public class APIEndpointObjectEntryModelListenerTest extends BaseTestCase {

	@Test
	public void testInvalidPathAPIEndpoint() throws Exception {
		JSONObject jsonObject = HTTPTestUtil.invoke(
			JSONUtil.put(
				"httpMethod", "get"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"path",
				StringBundler.concat(
					RandomTestUtil.randomString(), StringPool.FORWARD_SLASH,
					RandomTestUtil.randomString(), StringPool.COMMA)
			).put(
				"scope", "company"
			).toString(),
			"headless-builder/endpoints", Http.Method.POST);

		Assert.assertEquals("BAD_REQUEST", jsonObject.get("status"));
		Assert.assertEquals(
			"Path can have a maximum of 255 alphanumeric characters",
			jsonObject.get("title"));
	}

	@Test
	public void testPostAPIEndpointNotRelatedWithAPIApplication()
		throws Exception {

		JSONObject jsonObject = HTTPTestUtil.invoke(
			JSONUtil.put(
				"httpMethod", "get"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"path", RandomTestUtil.randomString()
			).put(
				"scope", "company"
			).toString(),
			"headless-builder/endpoints", Http.Method.POST);

		Assert.assertEquals("BAD_REQUEST", jsonObject.get("status"));
		Assert.assertEquals(
			"An endpoint must be related to an application",
			jsonObject.get("title"));
	}

	@Test
	public void testPostAPIEndpointRelatedWithAPIApplication()
		throws Exception {

		JSONObject apiApplicationJSONObject = _createAPIApplicationJSONObject();

		JSONObject jsonObject = HTTPTestUtil.invoke(
			JSONUtil.put(
				"httpMethod", "get"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"path", RandomTestUtil.randomString()
			).put(
				"r_apiApplicationToAPIEndpoints_c_apiApplicationId",
				apiApplicationJSONObject.getLong("id")
			).put(
				"scope", "company"
			).toString(),
			"headless-builder/endpoints", Http.Method.POST);

		Assert.assertEquals(
			0,
			jsonObject.getJSONObject(
				"status"
			).get(
				"code"
			));

		Assert.assertEquals(
			jsonObject.get("r_apiApplicationToAPIEndpoints_c_apiApplicationId"),
			apiApplicationJSONObject.get("id"));
	}

	@Test
	public void testPostAPIEndpointRelatedWithAPIApplicationWithSamePath()
		throws Exception {

		String path = RandomTestUtil.randomString();

		JSONObject apiApplicationJSONObject = _createAPIApplicationJSONObject();

		JSONObject jsonObject = HTTPTestUtil.invoke(
			JSONUtil.put(
				"httpMethod", "get"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"path", path
			).put(
				"r_apiApplicationToAPIEndpoints_c_apiApplicationId",
				apiApplicationJSONObject.getLong("id")
			).put(
				"scope", "company"
			).toString(),
			"headless-builder/endpoints", Http.Method.POST);

		Assert.assertEquals(
			jsonObject.get("r_apiApplicationToAPIEndpoints_c_apiApplicationId"),
			apiApplicationJSONObject.get("id"));

		jsonObject = HTTPTestUtil.invoke(
			JSONUtil.put(
				"httpMethod", "get"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"path", path
			).put(
				"r_apiApplicationToAPIEndpoints_c_apiApplicationId",
				apiApplicationJSONObject.getLong("id")
			).put(
				"scope", "company"
			).toString(),
			"headless-builder/endpoints", Http.Method.POST);

		Assert.assertEquals("BAD_REQUEST", jsonObject.get("status"));
		Assert.assertEquals(
			"There is an endpoint with the same http method and path " +
				"combination",
			jsonObject.get("title"));
	}

	@Test
	public void testValidPathAPIEndpoint() throws Exception {
		JSONObject apiApplicationJSONObject = _createAPIApplicationJSONObject();

		JSONObject jsonObject = HTTPTestUtil.invoke(
			JSONUtil.put(
				"httpMethod", "get"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"path", RandomTestUtil.randomString()
			).put(
				"r_apiApplicationToAPIEndpoints_c_apiApplicationId",
				apiApplicationJSONObject.getLong("id")
			).put(
				"scope", "company"
			).toString(),
			"headless-builder/endpoints", Http.Method.POST);

		Assert.assertEquals(
			0,
			jsonObject.getJSONObject(
				"status"
			).get(
				"code"
			));
	}

	private JSONObject _createAPIApplicationJSONObject() throws Exception {
		return HTTPTestUtil.invoke(
			JSONUtil.put(
				"applicationStatus", "published"
			).put(
				"baseURL", RandomTestUtil.randomString()
			).put(
				"title", RandomTestUtil.randomString()
			).toString(),
			"headless-builder/applications", Http.Method.POST);
	}

}