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

package com.liferay.segments.item.selector.web.internal;

import com.liferay.item.selector.ItemSelectorViewDescriptor;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.segments.model.SegmentsExperience;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
public class SegmentsExperienceItemDescriptor
	implements ItemSelectorViewDescriptor.ItemDescriptor {

	public SegmentsExperienceItemDescriptor(
		HttpServletRequest httpServletRequest,
		SegmentsExperience segmentsExperience) {

		_httpServletRequest = httpServletRequest;
		_segmentsExperience = segmentsExperience;
	}

	@Override
	public String getIcon() {
		return null;
	}

	@Override
	public String getImageURL() {
		return null;
	}

	@Override
	public Date getModifiedDate() {
		return _segmentsExperience.getModifiedDate();
	}

	@Override
	public String getPayload() {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return JSONUtil.put(
			"name", _segmentsExperience.getName(themeDisplay.getLocale())
		).put(
			"segmentsExperienceId",
			_segmentsExperience.getSegmentsExperienceId()
		).toString();
	}

	@Override
	public String getSubtitle(Locale locale) {
		return StringPool.BLANK;
	}

	@Override
	public String getTitle(Locale locale) {
		return HtmlUtil.escape(_segmentsExperience.getName(locale));
	}

	@Override
	public long getUserId() {
		return _segmentsExperience.getUserId();
	}

	@Override
	public String getUserName() {
		return _segmentsExperience.getUserName();
	}

	private final HttpServletRequest _httpServletRequest;
	private final SegmentsExperience _segmentsExperience;

}