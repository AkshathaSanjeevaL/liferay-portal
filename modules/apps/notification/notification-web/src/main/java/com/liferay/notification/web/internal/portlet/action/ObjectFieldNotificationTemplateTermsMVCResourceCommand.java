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

package com.liferay.notification.web.internal.portlet.action;

import com.liferay.notification.constants.NotificationPortletKeys;
import com.liferay.object.definition.notification.term.util.ObjectDefinitionNotificationTermUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Paulo Albuquerque
 */
@Component(
	property = {
		"javax.portlet.name=" + NotificationPortletKeys.NOTIFICATION_TEMPLATES,
		"mvc.command.name=/notification_templates/object_field_notification_template_terms"
	},
	service = MVCResourceCommand.class
)
public class ObjectFieldNotificationTemplateTermsMVCResourceCommand
	extends BaseNotificationTemplateTermsMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		_objectDefinition = _objectDefinitionLocalService.fetchObjectDefinition(
			ParamUtil.getLong(resourceRequest, "objectDefinitionId"));

		if (_objectDefinition == null) {
			return;
		}

		super.doServeResource(resourceRequest, resourceResponse);
	}

	@Override
	protected Set<Map.Entry<String, String>> getEntrySet() {
		List<ObjectField> objectFields =
			_objectFieldLocalService.getObjectFields(
				_objectDefinition.getObjectDefinitionId());

		Map<String, String> termValues = new LinkedHashMap<>();

		for (ObjectField objectField : objectFields) {
			if (StringUtil.equals(objectField.getName(), "creator") &&
				FeatureFlagManagerUtil.isEnabled("LPS-171625")) {

				termValues.putAll(_creatorTermValues);
			}
			else {
				termValues.put(
					objectField.getLabel(user.getLocale()),
					objectField.getName());
			}
		}

		return termValues.entrySet();
	}

	@Override
	protected String getTermName(String value) {
		return ObjectDefinitionNotificationTermUtil.getObjectFieldTermName(
			_objectDefinition.getShortName(), value);
	}

	private static final Map<String, String> _creatorTermValues =
		Collections.unmodifiableMap(
			LinkedHashMapBuilder.put(
				"author-email-address", "CREATOR_EMAIL"
			).put(
				"author-first-name", "CREATOR_FIRSTNAME"
			).put(
				"author-id", "CREATOR_ID"
			).put(
				"author-last-name", "CREATOR_LASTNAME"
			).put(
				"author-middle-name", "CREATOR_MIDDLENAME"
			).put(
				"author-prefix", "CREATOR_PREFIX"
			).put(
				"author-suffix", "CREATOR_SUFFIX"
			).build());

	private ObjectDefinition _objectDefinition;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

}