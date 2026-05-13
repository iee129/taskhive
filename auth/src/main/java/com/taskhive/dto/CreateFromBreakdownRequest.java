package com.taskhive.dto;

import java.util.List;

public record CreateFromBreakdownRequest(List<BrainDumpItem> items, Long projectId) {}
