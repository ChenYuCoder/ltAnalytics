package com.vichen;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConfigurationProperties(prefix = "analytics", ignoreUnknownFields = false)
public class LTAnalyticsConfig {
    private Set<String> recommendIdList;
    private Set<String> triggerList;
    private Set<String> huaweiInsideRecommendList;
    private String conventionIdPrefix;
    private Set<String> analyticsProvince;

    public Set<String> getRecommendIdList() {
        return recommendIdList;
    }

    public void setRecommendIdList(Set<String> recommendIdList) {
        this.recommendIdList = recommendIdList;
    }

    public Set<String> getTriggerList() {
        return triggerList;
    }

    public void setTriggerList(Set<String> triggerList) {
        this.triggerList = triggerList;
    }

    public Set<String> getHuaweiInsideRecommendList() {
        return huaweiInsideRecommendList;
    }

    public void setHuaweiInsideRecommendList(Set<String> huaweiInsideRecommendList) {
        this.huaweiInsideRecommendList = huaweiInsideRecommendList;
    }

    public String getConventionIdPrefix() {
        return conventionIdPrefix;
    }

    public void setConventionIdPrefix(String conventionIdPrefix) {
        this.conventionIdPrefix = conventionIdPrefix;
    }

    public Set<String> getAnalyticsProvince() {
        return analyticsProvince;
    }

    public void setAnalyticsProvince(Set<String> analyticsProvince) {
        this.analyticsProvince = analyticsProvince;
    }

    @Override
    public String toString() {
        return "LTAnalyticsConfig{" +
                "recommendIdList=" + recommendIdList +
                ", triggerList=" + triggerList +
                ", huaweiInsideRecommendList=" + huaweiInsideRecommendList +
                ", conventionIdPrefix='" + conventionIdPrefix + '\'' +
                ", analyticsProvince=" + analyticsProvince +
                '}';
    }
}
