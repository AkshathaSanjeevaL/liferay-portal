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

package com.liferay.portal.security.audit.event.generators.internal.security.auth;

import com.liferay.portal.kernel.audit.AuditException;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.AuthDNE;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Nikoletta Buza
 */
@Component(service = AuthDNE.class)
public class LoginAuthDNE implements AuthDNE {

	@Override
	public void onDoesNotExist(
		long companyId, String authType, String login,
		Map<String, String[]> headerMap, Map<String, String[]> parameterMap) {

		try {
			AuditMessage auditMessage = new AuditMessage(
				EventTypes.LOGIN_DNE, companyId, 0, null, User.class.getName(),
				"0", null,
				JSONUtil.put(
					"authType", authType
				).put(
					"headers", _jsonFactory.serialize(headerMap)
				).put(
					"reason", "User does not exist"
				));

			auditMessage.setUserLogin(login);

			if (auditMessage == null) {
				return;
			}

			_auditRouter.route(auditMessage);
		}
		catch (AuditException auditException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to route audit message", auditException);
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(LoginAuthDNE.class);

	@Reference
	private AuditRouter _auditRouter;

	@Reference
	private JSONFactory _jsonFactory;

}