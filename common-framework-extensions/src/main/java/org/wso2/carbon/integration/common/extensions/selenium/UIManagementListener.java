/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.common.extensions.selenium;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.integration.common.extensions.utils.ExtensionCommonConstants;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * UI Related activities during a test execution.
 * onTestFailure : It will capture the screen shot of the browser window and save it.
 */
public class UIManagementListener implements ITestListener {
    private static final Log log = LogFactory.getLog(UIManagementListener.class);

    @Override
    public void onTestStart(ITestResult iTestResult) {
        log.info("On test start :" + iTestResult.getTestClass().getName() + "." + iTestResult.getName());
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        log.info("On test success :" + iTestResult.getTestClass().getName() + "." + iTestResult.getName());
    }

    /**
     * Execute in a test failure situation.
     *
     * @param iTestResult Information about failure test case
     */
    @Override
    public void onTestFailure(ITestResult iTestResult) {
        String fullTestName = iTestResult.getTestClass().getName() + "." + iTestResult.getName();
        log.info("On test failure :" + fullTestName);
        doScreenCapture(fullTestName);
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        log.info("On test skipped :" + iTestResult.getTestClass().getName() + "." + iTestResult.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Override
    public void onStart(ITestContext iTestContext) {

    }

    @Override
    public void onFinish(ITestContext iTestContext) {

    }

    /**
     * Capture the current web browser screen and save it.
     *
     * @param fullTestName - Full name of the test case.
     */
    private void doScreenCapture(String fullTestName) {
        WebDriver webDriver = BrowserManager.driver;
        try {
            log.info("Screen capturing Start : " + fullTestName);
            // Retrieve report location of the Test Framework
            String reportLocation = FrameworkPathUtil.getReportLocation();
            long currentTime = System.currentTimeMillis();
            DateFormat dateFormat = new SimpleDateFormat(ExtensionCommonConstants.DATE_FORMAT_YY_MM_DD_HH_MIN_SS);
            Calendar cal = Calendar.getInstance();
            //Image path looks like :
            // [reportlocation]/capturedscreens/failedtests/[fullTestName]_[Date]_[Current time in milliseconds].png
            String imagePath =
                    reportLocation + File.separator + ExtensionCommonConstants.SCREEN_SHOT_LOCATION + File.separator +
                            fullTestName + "_" + dateFormat.format(cal.getTime()) + "_" + currentTime +
                            ExtensionCommonConstants.SCREEN_SHOT_EXTENSION;
            File scrFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(imagePath));
            log.info("Screen capturing End : " + fullTestName);
        } catch (IOException e) {
            //  Even having problems in screen shot  generation, test need to be continued hence not throwing any exceptions.
            log.warn("Error in screen capturing  for test failure in " + fullTestName, e);
        }
    }
}
