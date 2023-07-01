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

package com.liferay.ai.creator.openai.web.internal.display.context;

import com.liferay.ai.creator.openai.configuration.manager.AICreatorOpenAIConfigurationManager;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Lourdes Fernández Besada
 */
public class AICreatorOpenAIGroupConfigurationDisplayContext {

	public AICreatorOpenAIGroupConfigurationDisplayContext(
		AICreatorOpenAIConfigurationManager aiCreatorOpenAIConfigurationManager,
		HttpServletRequest httpServletRequest) {

		_aiCreatorOpenAIConfigurationManager =
			aiCreatorOpenAIConfigurationManager;
		_httpServletRequest = httpServletRequest;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIKey() throws ConfigurationException {
		String apiKey = ParamUtil.getString(
			_httpServletRequest, "apiKey", null);

		if (apiKey != null) {
			return apiKey;
		}

		return _aiCreatorOpenAIConfigurationManager.
			getAICreatorOpenAIGroupAPIKey(_themeDisplay.getScopeGroupId());
	}

	public boolean isCompanyEnabled() throws ConfigurationException {
		return _aiCreatorOpenAIConfigurationManager.
			isAICreatorOpenAICompanyEnabled(_themeDisplay.getCompanyId());
	}

	public boolean isEnabled() throws ConfigurationException {
		String enabled = ParamUtil.getString(
			_httpServletRequest, "enableOpenAI", null);

		if (enabled != null) {
			return GetterUtil.getBoolean(enabled);
		}

		return _aiCreatorOpenAIConfigurationManager.
			isAICreatorOpenAIGroupEnabled(
				_themeDisplay.getCompanyId(), _themeDisplay.getScopeGroupId());
	}

	private final AICreatorOpenAIConfigurationManager
		_aiCreatorOpenAIConfigurationManager;
	private final HttpServletRequest _httpServletRequest;
	private final ThemeDisplay _themeDisplay;

}