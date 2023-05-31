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

package com.liferay.layout.theme.item.selector.web.internal.context;

import com.liferay.layout.admin.constants.LayoutAdminPortletKeys;
import com.liferay.layout.theme.item.selector.web.internal.comparator.ThemeNameComparator;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.portlet.SearchOrderByUtil;
import com.liferay.portal.kernel.service.ThemeLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.display.context.GroupDisplayContextHelper;

import java.util.List;
import java.util.Objects;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Stefan Tanasie
 */
public class LayoutThemeItemSelectorDisplayContext {

	public LayoutThemeItemSelectorDisplayContext(
		HttpServletRequest httpServletRequest, RenderRequest renderRequest,
		PortletURL portletURL) {

		_httpServletRequest = httpServletRequest;
		_renderRequest = renderRequest;
		_portletURL = portletURL;
	}

	public String getOrderByCol() {
		if (Validator.isNotNull(_orderByCol)) {
			return _orderByCol;
		}

		_orderByCol = SearchOrderByUtil.getOrderByCol(
			_httpServletRequest, LayoutAdminPortletKeys.GROUP_PAGES,
			"select-order-by-col", "name");

		return _orderByCol;
	}

	public String getOrderByType() {
		if (Validator.isNotNull(_orderByType)) {
			return _orderByType;
		}

		_orderByType = SearchOrderByUtil.getOrderByType(
			_httpServletRequest, LayoutAdminPortletKeys.GROUP_PAGES,
			"select-order-by-type", "asc");

		return _orderByType;
	}

	public SearchContainer<Theme> getThemesSearchContainer() {
		if (_themesSearchContainer != null) {
			return _themesSearchContainer;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		SearchContainer<Theme> themesSearchContainer = new SearchContainer(
			_renderRequest, _portletURL, null, null);

		themesSearchContainer.setOrderByCol(getOrderByCol());
		themesSearchContainer.setOrderByType(getOrderByType());

		GroupDisplayContextHelper groupDisplayContextHelper =
			new GroupDisplayContextHelper(_httpServletRequest);

		boolean orderByAsc = false;

		if (Objects.equals(getOrderByType(), "asc")) {
			orderByAsc = true;
		}

		List<Theme> themes = ListUtil.sort(
			ThemeLocalServiceUtil.getPageThemes(
				themeDisplay.getCompanyId(),
				groupDisplayContextHelper.getLiveGroupId(),
				themeDisplay.getUserId()),
			new ThemeNameComparator(orderByAsc));

		themesSearchContainer.setResultsAndTotal(() -> themes, themes.size());

		_themesSearchContainer = themesSearchContainer;

		return _themesSearchContainer;
	}

	private final HttpServletRequest _httpServletRequest;
	private String _orderByCol;
	private String _orderByType;
	private final PortletURL _portletURL;
	private final RenderRequest _renderRequest;
	private SearchContainer<Theme> _themesSearchContainer;

}