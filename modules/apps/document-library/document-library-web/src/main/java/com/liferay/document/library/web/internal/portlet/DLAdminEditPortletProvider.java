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

package com.liferay.document.library.web.internal.portlet;

import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.portal.kernel.portlet.BasePortletProvider;
import com.liferay.portal.kernel.portlet.PortletProvider;

import org.osgi.service.component.annotations.Component;

/**
 * @author Sergio González
 */
@Component(
	property = {
		"model.class.name=com.liferay.document.library.kernel.model.DLFileEntryType",
		"model.class.name=com.liferay.portal.kernel.repository.model.FileEntry",
		"model.class.name=com.liferay.portal.kernel.repository.model.FileShortcut",
		"model.class.name=com.liferay.portal.kernel.repository.model.Folder"
	},
	service = PortletProvider.class
)
public class DLAdminEditPortletProvider extends BasePortletProvider {

	@Override
	public String getPortletName() {
		return DLPortletKeys.DOCUMENT_LIBRARY_ADMIN;
	}

	@Override
	public Action[] getSupportedActions() {
		return _supportedActions;
	}

	private final Action[] _supportedActions = {Action.MANAGE};

}