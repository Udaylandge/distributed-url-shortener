package com.uday.urlshortener.dto;

/**
 * Data Transfer Object for dashboard statistics.
 * Carries aggregated metrics for the dashboard summary cards.
 */
public class DashboardStatsDto {

    private long totalUrls;
    private long totalClicks;
    private long activeUrls;
    private long expiredUrls;
    private long todaysUrls;
    private long qrGenerated;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalUrls, long totalClicks, long activeUrls,
                              long expiredUrls, long todaysUrls, long qrGenerated) {
        this.totalUrls = totalUrls;
        this.totalClicks = totalClicks;
        this.activeUrls = activeUrls;
        this.expiredUrls = expiredUrls;
        this.todaysUrls = todaysUrls;
        this.qrGenerated = qrGenerated;
    }

    public long getTotalUrls() { return totalUrls; }
    public void setTotalUrls(long totalUrls) { this.totalUrls = totalUrls; }

    public long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }

    public long getActiveUrls() { return activeUrls; }
    public void setActiveUrls(long activeUrls) { this.activeUrls = activeUrls; }

    public long getExpiredUrls() { return expiredUrls; }
    public void setExpiredUrls(long expiredUrls) { this.expiredUrls = expiredUrls; }

    public long getTodaysUrls() { return todaysUrls; }
    public void setTodaysUrls(long todaysUrls) { this.todaysUrls = todaysUrls; }

    public long getQrGenerated() { return qrGenerated; }
    public void setQrGenerated(long qrGenerated) { this.qrGenerated = qrGenerated; }
}
