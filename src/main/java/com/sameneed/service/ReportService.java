package com.sameneed.service;

import com.sameneed.dao.ReportDao;
import com.sameneed.enums.ReportStatus;
import com.sameneed.exception.InvalidRequestException;
import com.sameneed.model.Report;

import java.util.List;

public class ReportService {

    private final ReportDao reportDao = new ReportDao();

    public Report fileReport(int reporterId, String targetType, int targetId, String reason) {
        if (targetType == null || (!targetType.equals("USER") && !targetType.equals("PROVIDER") && !targetType.equals("REQUEST"))) {
            throw new InvalidRequestException("Invalid target type");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidRequestException("Reason for report is required");
        }

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason.trim());
        report.setStatus(ReportStatus.OPEN);

        int reportId = reportDao.insert(report);
        report.setReportId(reportId);
        return report;
    }

    public List<Report> getAllReports() {
        return reportDao.findAll();
    }

    public void resolveReport(int reportId, String resolution) {
        ReportStatus status;
        if ("RESOLVED".equalsIgnoreCase(resolution)) {
            status = ReportStatus.RESOLVED;
        } else if ("DISMISSED".equalsIgnoreCase(resolution)) {
            status = ReportStatus.DISMISSED;
        } else if ("UNDER_REVIEW".equalsIgnoreCase(resolution)) {
            status = ReportStatus.UNDER_REVIEW;
        } else {
            throw new InvalidRequestException("Invalid resolution status");
        }
        reportDao.updateStatus(reportId, status);
    }

    public int countOpenReports() {
        return reportDao.countOpen();
    }
}
