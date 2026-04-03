package com.restfulbooker.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExtentReportManager {

    private static final String REPORT_DIRECTORY = "test-output/extent-report";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static ExtentReports extentReports;
    private static Path reportPath;

    private ExtentReportManager() {
    }

    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            extentReports = createInstance();
        }
        return extentReports;
    }

    public static synchronized Path getReportPath() {
        if (reportPath == null) {
            getInstance();
        }
        return reportPath;
    }

    private static ExtentReports createInstance() {
        try {
            Path reportDirectory = Paths.get(REPORT_DIRECTORY);
            Files.createDirectories(reportDirectory);

            reportPath = reportDirectory.resolve(
                    "ExtentReport_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".html"
            );

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath.toString());
            sparkReporter.config().setReportName("Restful Booker API Automation Report");
            sparkReporter.config().setDocumentTitle("Restful Booker Test Execution");

            ExtentReports reports = new ExtentReports();
            reports.attachReporter(sparkReporter);
            reports.setSystemInfo("Project", "API Automation Portfolio");
            reports.setSystemInfo("Framework", "RestAssured + Java + TestNG + Maven");
            reports.setSystemInfo("Base URL", "https://restful-booker.herokuapp.com");
            reports.setSystemInfo("Environment", System.getProperty("os.name"));
            reports.setSystemInfo("Java Version", System.getProperty("java.version"));
            return reports;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create Extent Report directory.", exception);
        }
    }
}
