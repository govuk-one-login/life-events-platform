package uk.gov.gdx.datashare

import org.approvaltests.core.ApprovalFailureReporter
import org.approvaltests.reporters.AutoApproveWhenEmptyReporter
import org.approvaltests.reporters.JunitReporter

class PackageSettings {
  var UseReporter: ApprovalFailureReporter = AutoApproveWhenEmptyReporter(JunitReporter())
  var FrontloadedReporter: ApprovalFailureReporter = AutoApproveWhenEmptyReporter(JunitReporter())
  var UseApprovalSubdirectory = "approvals"
  var ApprovalBaseDirectory = ""
}
