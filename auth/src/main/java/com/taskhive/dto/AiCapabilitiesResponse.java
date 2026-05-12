package com.taskhive.dto;

public record AiCapabilitiesResponse(boolean enabled, String provider, boolean cloudProvider) {
}
