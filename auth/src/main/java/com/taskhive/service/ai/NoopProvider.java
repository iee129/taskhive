package com.taskhive.service.ai;

public class NoopProvider implements AiProvider {

    @Override
    public String generate(String prompt) {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getProviderName() {
        return "none";
    }

    @Override
    public boolean isCloudProvider() {
        return false;
    }
}
