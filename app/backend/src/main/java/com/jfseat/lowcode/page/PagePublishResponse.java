package com.jfseat.lowcode.page;

public record PagePublishResponse(
        String pageCode,
        int version,
        String message
) {
}
