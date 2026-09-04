package com.jfseat.lowcode.page;

import java.time.Instant;

public record PageSummary(String pageCode, String pageName, int currentVersion, Instant updatedAt) {
}
