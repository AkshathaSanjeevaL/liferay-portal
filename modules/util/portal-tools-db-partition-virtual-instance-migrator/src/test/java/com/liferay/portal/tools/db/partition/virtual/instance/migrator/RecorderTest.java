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

package com.liferay.portal.tools.db.partition.virtual.instance.migrator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Luis Ortiz
 */
public class RecorderTest {

	@Before
	public void setUp() {
		System.setOut(new PrintStream(_byteArrayOutputStream));
	}

	@After
	public void tearDown() {
		System.setOut(_originalOut);
	}

	@Test
	public void testPrintingOrder() {
		Recorder recorder = new Recorder();

		recorder.registerWarning("A simple warning message");
		recorder.registerError("A simple error message");
		recorder.printMessages();

		Assert.assertTrue(recorder.hasWarnings());
		Assert.assertTrue(recorder.hasErrors());

		String outputString = _byteArrayOutputStream.toString();

		Assert.assertTrue(
			outputString.contains("[WARN] A simple warning message"));
		Assert.assertTrue(
			outputString.contains("[ERROR] A simple error message"));

		Assert.assertTrue(
			outputString.indexOf("[ERROR]") < outputString.indexOf("[WARN]"));
	}

	@Test
	public void testRegisterError() {
		Recorder recorder = new Recorder();

		recorder.registerError("A simple message");

		recorder.printMessages();

		Assert.assertFalse(recorder.hasWarnings());
		Assert.assertTrue(recorder.hasErrors());

		Assert.assertEquals(
			"[ERROR] A simple message\n", _byteArrayOutputStream.toString());
	}

	@Test
	public void testRegisterErrors() {
		Recorder recorder = new Recorder();

		List<String> modules = Arrays.asList("module1", "module2", "module3");

		recorder.registerErrors("simple message", modules);

		recorder.printMessages();

		Assert.assertFalse(recorder.hasWarnings());
		Assert.assertTrue(recorder.hasErrors());

		String outputString = _byteArrayOutputStream.toString();

		for (String module : modules) {
			Assert.assertTrue(
				outputString.contains(
					"[ERROR] Module " + module + " simple message"));
		}
	}

	@Test
	public void testRegisterWarning() {
		Recorder recorder = new Recorder();

		recorder.registerWarning("A simple message");

		recorder.printMessages();

		Assert.assertTrue(recorder.hasWarnings());
		Assert.assertFalse(recorder.hasErrors());

		Assert.assertEquals(
			"[WARN] A simple message\n", _byteArrayOutputStream.toString());
	}

	@Test
	public void testRegisterWarnings() {
		Recorder recorder = new Recorder();

		List<String> modules = Arrays.asList("module1", "module2", "module3");

		recorder.registerWarnings("simple message", modules);

		recorder.printMessages();

		Assert.assertTrue(recorder.hasWarnings());
		Assert.assertFalse(recorder.hasErrors());

		String outputString = _byteArrayOutputStream.toString();

		for (String module : modules) {
			Assert.assertTrue(
				outputString.contains(
					"[WARN] Module " + module + " simple message"));
		}
	}

	private final ByteArrayOutputStream _byteArrayOutputStream =
		new ByteArrayOutputStream();
	private final PrintStream _originalOut = System.out;

}