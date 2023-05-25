/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.testing.junitvintage

import org.gradle.integtests.fixtures.TargetCoverage
import org.gradle.testing.junit4.AbstractJUnit4LoggingOutputCaptureIntegrationTest

import static org.gradle.testing.fixture.JUnitCoverage.JUNIT_VINTAGE

// https://github.com/junit-team/junit5/issues/1285
@TargetCoverage({ JUNIT_VINTAGE })
class JUnitVintageLoggingOutputCaptureIntegrationTest extends AbstractJUnit4LoggingOutputCaptureIntegrationTest implements JUnitVintageMultiVersionTest {
}