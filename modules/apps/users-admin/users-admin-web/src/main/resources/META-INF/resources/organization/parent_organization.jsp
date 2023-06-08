<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
long organizationId = ParamUtil.getLong(request, "organizationId");

Organization organization = OrganizationServiceUtil.fetchOrganization(organizationId);

long parentOrganizationId = ParamUtil.getLong(request, "parentOrganizationId", (organization != null) ? organization.getParentOrganizationId() : OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID);

if (parentOrganizationId <= 0) {
	parentOrganizationId = OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID;

	if (organization != null) {
		parentOrganizationId = organization.getParentOrganizationId();
	}
}

User selUser = (User)request.getAttribute("user.selUser");

Organization parentOrganization = null;

if ((organization == null) && (parentOrganizationId == OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID) && !permissionChecker.isCompanyAdmin()) {
	List<Organization> manageableOrganizations = new ArrayList<Organization>();

	for (Organization curOrganization : selUser.getOrganizations()) {
		if (OrganizationPermissionUtil.contains(permissionChecker, curOrganization, ActionKeys.MANAGE_SUBORGANIZATIONS)) {
			manageableOrganizations.add(curOrganization);
		}
	}

	if (manageableOrganizations.size() == 1) {
		Organization manageableOrganization = manageableOrganizations.get(0);

		parentOrganizationId = manageableOrganization.getOrganizationId();
	}
}

if (parentOrganizationId != OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID) {
	try {
		parentOrganization = OrganizationLocalServiceUtil.getOrganization(parentOrganizationId);
	}
	catch (NoSuchOrganizationException nsoe) {
	}
}
%>

<liferay-ui:error exception="<%= OrganizationParentException.class %>" message="please-enter-a-valid-parent-organization" />

<liferay-ui:error exception="<%= OrganizationParentException.MustBeRootable.class %>">

	<%
	OrganizationParentException.MustBeRootable mbr = (OrganizationParentException.MustBeRootable)errorException;
	%>

	<liferay-ui:message arguments="<%= mbr.getType() %>" key="an-organization-of-type-x-cannot-be-a-root-organization" />
</liferay-ui:error>

<liferay-ui:error exception="<%= OrganizationParentException.MustHaveValidChildType.class %>">

	<%
	OrganizationParentException.MustHaveValidChildType mhvct = (OrganizationParentException.MustHaveValidChildType)errorException;
	%>

	<liferay-ui:message arguments="<%= new String[] {mhvct.getChildOrganizationType(), mhvct.getParentOrganizationType()} %>" key="an-organization-of-type-x-is-not-allowed-as-a-child-of-type-x" />
</liferay-ui:error>

<liferay-ui:error exception="<%= OrganizationParentException.MustNotHaveChildren.class %>">

	<%
	OrganizationParentException.MustNotHaveChildren mnhc = (OrganizationParentException.MustNotHaveChildren)errorException;
	%>

	<liferay-ui:message arguments="<%= mnhc.getType() %>" key="an-organization-of-type-x-cannot-have-children" />
</liferay-ui:error>

<portlet:renderURL var="selectOrganizationRenderURL" windowState="<%= LiferayWindowState.POP_UP.toString() %>">
	<portlet:param name="p_u_i_d" value='<%= (selUser == null) ? "0" : String.valueOf(selUser.getUserId()) %>' />
	<portlet:param name="mvcPath" value="/select_organization.jsp" />
	<portlet:param name="organizationId" value="<%= String.valueOf(organizationId) %>" />
</portlet:renderURL>

<div>
	<react:component
		module="js/ParentOrganization"
		props='<%=
			HashMapBuilder.<String, Object>put(
				"label", (parentOrganization != null) ? parentOrganization.getName() : ""
			).put(
				"parentOrganizationId", (parentOrganization != null) ? parentOrganization.getOrganizationId() : ""
			).put(
				"selectOrganizationRenderURL", selectOrganizationRenderURL.toString()
			).build()
		%>'
	/>
</div>