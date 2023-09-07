package uk.gov.di.data.lep;

import org.approvaltests.core.ApprovalFailureReporter;
import org.approvaltests.reporters.JunitReporter;
import org.approvaltests.reporters.AutoApproveWhenEmptyReporter;

public class PackageSettings {
    public static ApprovalFailureReporter UseReporter = new AutoApproveWhenEmptyReporter(new JunitReporter());
    public static ApprovalFailureReporter FrontloadedReporter = new AutoApproveWhenEmptyReporter(new JunitReporter());
    public static String UseApprovalSubdirectory = "approvals";
    public static String ApprovalBaseDirectory = "../resources";
}
