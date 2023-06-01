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

package com.liferay.user.groups.admin.item.selector.web.internal.display.context;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.service.UserGroupLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portlet.usergroupsadmin.search.UserGroupDisplayTerms;
import com.liferay.portlet.usergroupsadmin.search.UserGroupSearch;
import com.liferay.user.groups.admin.item.selector.UserGroupItemSelectorCriterion;
import com.liferay.user.groups.admin.item.selector.web.internal.search.UserGroupItemSelectorChecker;
import com.liferay.users.admin.kernel.util.UsersAdmin;
import com.liferay.users.admin.kernel.util.UsersAdminUtil;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Alessio Antonio Rendina
 */
public class UserGroupItemSelectorViewDisplayContext {

	public UserGroupItemSelectorViewDisplayContext(
		UserGroupLocalService userGroupLocalService,
		UserGroupItemSelectorCriterion userGroupItemSelectorCriterion,
		UsersAdmin usersAdmin, HttpServletRequest httpServletRequest,
		PortletURL portletURL, String itemSelectedEventName) {

		_userGroupLocalService = userGroupLocalService;
		_userGroupItemSelectorCriterion = userGroupItemSelectorCriterion;
		_usersAdmin = usersAdmin;
		_httpServletRequest = httpServletRequest;
		_portletURL = portletURL;
		_itemSelectedEventName = itemSelectedEventName;

		_renderRequest = (RenderRequest)httpServletRequest.getAttribute(
			JavaConstants.JAVAX_PORTLET_REQUEST);
		_renderResponse = (RenderResponse)httpServletRequest.getAttribute(
			JavaConstants.JAVAX_PORTLET_RESPONSE);
		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getItemSelectedEventName() {
		return _itemSelectedEventName;
	}

	public String getOrderByCol() {
		return ParamUtil.getString(
			_renderRequest, SearchContainer.DEFAULT_ORDER_BY_COL_PARAM, "name");
	}

	public String getOrderByType() {
		return ParamUtil.getString(
			_renderRequest, SearchContainer.DEFAULT_ORDER_BY_TYPE_PARAM, "asc");
	}

	public PortletURL getPortletURL() {
		return _portletURL;
	}

	public SearchContainer<UserGroup> getSearchContainer()
		throws PortalException {

		if (_searchContainer != null) {
			return _searchContainer;
		}

		_searchContainer = new UserGroupSearch(_renderRequest, getPortletURL());

		_searchContainer.setOrderByCol(getOrderByCol());
		_searchContainer.setOrderByComparator(
			_usersAdmin.getUserGroupOrderByComparator(
				getOrderByCol(), getOrderByType()));
		_searchContainer.setOrderByType(getOrderByType());

		long companyId = CompanyThreadLocal.getCompanyId();

		UserGroupDisplayTerms searchTerms =
			(UserGroupDisplayTerms)_searchContainer.getSearchTerms();

		String keywords = searchTerms.getKeywords();

		if (_userGroupItemSelectorCriterion.isFilterManageableUserGroups()) {
			_searchContainer.setResultsAndTotal(
				UsersAdminUtil.filterUserGroups(
					_themeDisplay.getPermissionChecker(),
					UserGroupLocalServiceUtil.search(
						_themeDisplay.getCompanyId(), keywords, null,
						QueryUtil.ALL_POS, QueryUtil.ALL_POS,
						_searchContainer.getOrderByComparator())));
		}
		else {
			_searchContainer.setResultsAndTotal(
				() -> _userGroupLocalService.search(
					companyId, keywords, null, _searchContainer.getStart(),
					_searchContainer.getEnd(),
					_searchContainer.getOrderByComparator()),
				_userGroupLocalService.searchCount(companyId, keywords, null));
		}

		_searchContainer.setRowChecker(
			new UserGroupItemSelectorChecker(
				_renderResponse, _getCheckedUserGroupIds()));

		return _searchContainer;
	}

	private long[] _getCheckedUserGroupIds() {
		return ParamUtil.getLongValues(_renderRequest, "checkedUserGroupIds");
	}

	private final HttpServletRequest _httpServletRequest;
	private final String _itemSelectedEventName;
	private final PortletURL _portletURL;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private SearchContainer<UserGroup> _searchContainer;
	private final ThemeDisplay _themeDisplay;
	private final UserGroupItemSelectorCriterion
		_userGroupItemSelectorCriterion;
	private final UserGroupLocalService _userGroupLocalService;
	private final UsersAdmin _usersAdmin;

}