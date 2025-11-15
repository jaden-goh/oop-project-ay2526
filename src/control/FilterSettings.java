package control;

import java.util.Date;

public class FilterSettings {
    private final String userId;
    private String status = "All";
    private String major = "All";
    private String level = "All";
    private String company = "All";
    private Date closingStart;
    private Date closingEnd;

    public FilterSettings(String userId) {
        this.userId = userId == null || userId.isBlank() ? "anonymous" : userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getMajor() {
        return major;
    }

    public String getLevel() {
        return level;
    }

    public String getCompany() {
        return company;
    }

    public Date getClosingStart() {
        return closingStart;
    }

    public Date getClosingEnd() {
        return closingEnd;
    }

    public void update(String status, String major, String level, String company, Date closingStart, Date closingEnd) {
        this.status = toFilterValue(status, this.status);
        this.major = toFilterValue(major, this.major);
        this.level = toFilterValue(level, this.level);
        this.company = toFilterValue(company, this.company);
        if (closingStart != null) {
            this.closingStart = closingStart;
        }
        if (closingEnd != null) {
            this.closingEnd = closingEnd;
        }
    }

    private String toFilterValue(String candidate, String fallback) {
        if (candidate == null || candidate.isBlank()) {
            return fallback != null ? fallback : "All";
        }
        return candidate;
    }

}
