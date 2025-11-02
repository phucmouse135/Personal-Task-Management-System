package org.example.cv.constants;

public enum CacheConstant {
    // project caches
    PROJECT_DETAIL("project-detail"),
    PROJECT_LIST("project-list"),
    // task caches
    CACHE_TASK_DETAILS("cache-task-details"),
    CACHE_TASK_LISTS("cache-task-lists");

    private final String cacheName;

    CacheConstant(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheName() {
        return cacheName;
    }
}
