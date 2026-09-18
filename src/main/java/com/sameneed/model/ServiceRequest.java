package com.sameneed.model;

import com.sameneed.enums.RequestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ServiceRequest {

    private int requestId;
    private int creatorId;
    private String creatorName;
    private int serviceId;
    private String serviceName;
    private String categoryName;
    private String problemDescription;
    private String locality;
    private LocalDate preferredDate;
    private LocalTime preferredTime;
    private BigDecimal budgetExpectation;
    private int maxGroupSize;
    private int currentMemberCount;
    private RequestStatus status;
    private LocalDateTime createdAt;

    public ServiceRequest() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }

    public LocalTime getPreferredTime() { return preferredTime; }
    public void setPreferredTime(LocalTime preferredTime) { this.preferredTime = preferredTime; }

    public BigDecimal getBudgetExpectation() { return budgetExpectation; }
    public void setBudgetExpectation(BigDecimal budgetExpectation) { this.budgetExpectation = budgetExpectation; }

    public int getMaxGroupSize() { return maxGroupSize; }
    public void setMaxGroupSize(int maxGroupSize) { this.maxGroupSize = maxGroupSize; }

    public int getCurrentMemberCount() { return currentMemberCount; }
    public void setCurrentMemberCount(int currentMemberCount) { this.currentMemberCount = currentMemberCount; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isOpen() {
        return status == RequestStatus.REQUESTED || status == RequestStatus.GROUP_FORMING;
    }

    public boolean hasCapacity() {
        return currentMemberCount < maxGroupSize;
    }
}
