/*
Copyright 2015 Electric Cloud, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package test.java.ecplugins.azure;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class RunTest {

	// configurations;
			@BeforeClass
			public static void setUpBeforeClass() throws Exception {
				TestUtils.setUpBeforeClass(RunTest.class.getSimpleName().replace("Test", ""));
			}

			@Test
			public void test_a_create() throws Exception {
				TestUtils.test(StringConstants.CREATE);
			}
			@Test
			public void test_b_start() throws Exception {
				TestUtils.test(StringConstants.START);
			}
			@Test
			public void test_c_restart() throws Exception {
				TestUtils.test(StringConstants.RESTART);
			}
			@Test
			public void test_d_stop() throws Exception {
				TestUtils.test(StringConstants.STOP);
			}
			@Test
			public void test_e_delete() throws Exception {
				TestUtils.test(StringConstants.DELETE);
			}
}