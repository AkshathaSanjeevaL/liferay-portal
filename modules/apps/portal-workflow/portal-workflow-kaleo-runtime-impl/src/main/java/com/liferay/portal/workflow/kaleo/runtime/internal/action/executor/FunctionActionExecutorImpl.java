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

package com.liferay.portal.workflow.kaleo.runtime.internal.action.executor;

import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.object.model.ObjectEntry;
import com.liferay.osgi.util.configuration.ConfigurationFactoryUtil;
import com.liferay.portal.catapult.PortalCatapult;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.kernel.workflow.WorkflowTaskAssignee;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.workflow.kaleo.model.KaleoAction;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.action.executor.ActionExecutor;
import com.liferay.portal.workflow.kaleo.runtime.action.executor.ActionExecutorException;
import com.liferay.portal.workflow.kaleo.runtime.internal.configuration.FunctionActionExecutorImplConfiguration;
import com.liferay.portal.workflow.kaleo.runtime.util.ScriptingContextBuilder;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Raymond Augé
 */
@Component(
	configurationPid = "com.liferay.portal.workflow.kaleo.runtime.internal.configuration.FunctionActionExecutorImplConfiguration",
	factory = "com.liferay.portal.workflow.kaleo.runtime.internal.action.executor.FunctionActionExecutorImpl",
	service = ActionExecutor.class
)
public class FunctionActionExecutorImpl implements ActionExecutor {

	public static final String KEY = "actionExecutorLanguage";

	@Override
	public void execute(
			KaleoAction kaleoAction, ExecutionContext executionContext)
		throws ActionExecutorException {

		try {
			doExecute(kaleoAction, executionContext);
		}
		catch (Exception exception) {
			throw new ActionExecutorException(exception);
		}
	}

	@Override
	public String getActionExecutorKey() {
		return _actionExecutorKey;
	}

	@Activate
	protected void activate(Map<String, Object> properties) throws Exception {
		_actionExecutorKey = (String)properties.get(KEY);
		_companyId = ConfigurationFactoryUtil.getCompanyId(
			_companyLocalService, properties);
		_functionActionExecutorImplConfiguration =
			ConfigurableUtil.createConfigurable(
				FunctionActionExecutorImplConfiguration.class, properties);
	}

	protected void doExecute(
			KaleoAction kaleoAction, ExecutionContext executionContext)
		throws Exception {

		JSONObject payloadJSONObject = _jsonFactory.createJSONObject();

		Map<String, Object> inputObjects =
			_scriptingContextBuilder.buildScriptingContext(executionContext);

		for (Map.Entry<String, Object> entry : inputObjects.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof Number || value instanceof String) {
				payloadJSONObject.put(key, value);

				continue;
			}

			JSONObject jsonObject = _jsonFactory.createJSONObject(
				_jsonFactory.serialize(value));

			if (jsonObject.has("javaClass")) {
				if (jsonObject.has("list")) {
					payloadJSONObject.put(key, jsonObject.getJSONArray("list"));
				}
				else if (jsonObject.has("map")) {
					payloadJSONObject.put(key, jsonObject.getJSONObject("map"));
				}
				else if (jsonObject.has("serializable")) {
					payloadJSONObject.put(
						key, jsonObject.getJSONObject("serializable"));
				}
				else {
					payloadJSONObject.put(key, jsonObject);
				}
			}
			else {
				payloadJSONObject.put(key, jsonObject);
			}
		}

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			executionContext.getKaleoTaskInstanceToken();

		long workflowTaskId =
			kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId();

		payloadJSONObject.put(
			"nextTransitionNames",
			_workflowTaskManager.getNextTransitionNames(
				kaleoAction.getUserId(), workflowTaskId)
		).put(
			"transitionURL",
			"/o/headless-admin-workflow/v1.0/workflow-tasks/" + workflowTaskId +
				"/change-transition"
		).put(
			"workflowTaskId", workflowTaskId
		);

		payloadJSONObject.remove("serviceContext");
		payloadJSONObject.remove("workflowContext");

		JSONObject kaleoTaskInstanceTokenJSONObject =
			payloadJSONObject.getJSONObject("kaleoTaskInstanceToken");

		kaleoTaskInstanceTokenJSONObject.remove("workflowContext");

		List<WorkflowTaskAssignee> workflowTaskAssignees =
			(List<WorkflowTaskAssignee>)inputObjects.get(
				"workflowTaskAssignees");

		if (workflowTaskAssignees.isEmpty()) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"There are no workflow task assignees for Kaleo action " +
						kaleoAction);
			}

			return;
		}

		WorkflowTaskAssignee workflowTaskAssignee = workflowTaskAssignees.get(
			0);

		if (!Objects.equals(
				workflowTaskAssignee.getAssigneeClassName(),
				User.class.getName())) {

			if (_log.isWarnEnabled()) {
				_log.warn(
					"The first workflow task assignee must be a user for " +
						"Kaleo action " + kaleoAction);
			}

			return;
		}

		payloadJSONObject.put(
			"entryDTO",
			_getEntryDTOJSONObject(
				(String)inputObjects.get(
					WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME),
				GetterUtil.getLong(
					inputObjects.get(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK)),
				executionContext));

		_portalCatapult.launch(
			_companyId, Http.Method.POST,
			_functionActionExecutorImplConfiguration.
				oAuth2ApplicationExternalReferenceCode(),
			payloadJSONObject,
			_functionActionExecutorImplConfiguration.resourcePath(),
			workflowTaskAssignee.getAssigneeClassPK());
	}

	private JSONObject _getEntryDTOJSONObject(
			String className, long classPK, ExecutionContext executionContext)
		throws Exception {

		WorkflowHandler<?> workflowHandler =
			WorkflowHandlerRegistryUtil.getWorkflowHandler(className);

		AssetRenderer<?> assetRenderer = workflowHandler.getAssetRenderer(
			classPK);

		BaseModel<?> baseModel = (BaseModel<?>)assetRenderer.getAssetObject();

		if ((assetRenderer == null) || (baseModel == null)) {
			return null;
		}

		String dtoclassName = assetRenderer.getClassName();

		if (baseModel instanceof ObjectEntry) {
			dtoclassName = ObjectEntry.class.getName();
		}

		DTOConverter<Serializable, Serializable> dtoConverter =
			(DTOConverter<Serializable, Serializable>)
				_dtoConverterRegistry.getDTOConverter(dtoclassName);

		ServiceContext serviceContext = executionContext.getServiceContext();

		Serializable serializable = dtoConverter.toDTO(
			new DefaultDTOConverterContext(
				false, null, null, null, serviceContext.getLocale(), null,
				null),
			baseModel);

		JSONObject entryDTOJSONObject = _jsonFactory.createJSONObject(
			serializable.toString());

		if (!(baseModel instanceof ObjectEntry)) {
			return entryDTOJSONObject;
		}

		JSONObject propertiesJSONObject = entryDTOJSONObject.getJSONObject(
			"properties");

		for (String key : propertiesJSONObject.keySet()) {
			entryDTOJSONObject.put(key, propertiesJSONObject.get(key));
		}

		entryDTOJSONObject.remove("properties");

		return entryDTOJSONObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FunctionActionExecutorImpl.class);

	private String _actionExecutorKey;
	private long _companyId;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	private FunctionActionExecutorImplConfiguration
		_functionActionExecutorImplConfiguration;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private PortalCatapult _portalCatapult;

	@Reference
	private ScriptingContextBuilder _scriptingContextBuilder;

	@Reference
	private WorkflowTaskManager _workflowTaskManager;

}