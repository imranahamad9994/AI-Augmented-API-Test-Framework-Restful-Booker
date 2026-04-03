package com.restfulbooker.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.restfulbooker.reports.ExtentReportManager;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExtentReportListener implements ITestListener, ISuiteListener {

    private static final ThreadLocal<ExtentTest> TEST_LOG = new ThreadLocal<>();

    private ExtentReports extentReports;

    @Override
    public void onStart(ISuite suite) {
        extentReports = ExtentReportManager.getInstance();
    }

    @Override
    public void onFinish(ISuite suite) {
        if (extentReports != null) {
            extentReports.flush();
            openReportAutomatically();
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extentReports.createTest(result.getMethod().getMethodName());
        extentTest.assignCategory(result.getTestClass().getName());
        extentTest.info(result.getMethod().getDescription());
        TEST_LOG.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest extentTest = TEST_LOG.get();
        if (extentTest != null) {
            extentTest.log(Status.PASS, "Test passed successfully.");
        }
        TEST_LOG.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest extentTest = TEST_LOG.get();
        if (extentTest != null) {
            extentTest.log(Status.FAIL, result.getThrowable());
        }
        TEST_LOG.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest extentTest = TEST_LOG.get();
        if (extentTest != null) {
            extentTest.log(Status.SKIP, "Test was skipped.");
        }
        TEST_LOG.remove();
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }

    private void openReportAutomatically() {
        Path reportPath = ExtentReportManager.getReportPath();
        if (reportPath == null || !Files.exists(reportPath)) {
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            return;
        }

        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(reportPath.toFile());
                return;
            }
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(reportPath.toUri());
            }
        } catch (IOException exception) {
            System.err.println("Unable to open Extent Report automatically: " + exception.getMessage());
        }
    }
}
