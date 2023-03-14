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

package com.liferay.partner.services;

import com.sforce.async.BulkConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Jair Medeiros
 * @author Thaynam Lázaro
 */
@Service
public class SalesforceService {

	public void getBulkObjects() {
		System.out.println("Salesforce BulkConnection: " + _bulkConnection);
	}

	@Autowired
	private BulkConnection _bulkConnection;

}