/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.reports.engine.console.model.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.reports.engine.console.service.DefinitionLocalServiceUtil;

/**
 * @author Brian Wing Shun Chan
 */
public class DefinitionImpl extends DefinitionBaseImpl {

	@Override
	public String getAttachmentsDir() {
		return "reports_templates/".concat(String.valueOf(getDefinitionId()));
	}

	@Override
	public String[] getAttachmentsFileNames() throws PortalException {
		return DefinitionLocalServiceUtil.getAttachmentsFileNames(this);
	}

}