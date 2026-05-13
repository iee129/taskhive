package com.taskhive.service.ai;

public interface AiProvider {
    String generate(String prompt);
    boolean isAvailable();
    String getProviderName();
    boolean isCloudProvider();
}
