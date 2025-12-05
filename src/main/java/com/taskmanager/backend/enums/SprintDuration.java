package com.taskmanager.backend.enums;

import lombok.Getter;

@Getter
public enum SprintDuration {
    ONE_WEEK(1),
    TWO_WEEKS(2),
    THREE_WEEKS(3),
    FOUR_WEEKS(4),
    CUSTOM(0); // 0 tuần -> User tự chọn ngày

    private final int weeks;

    SprintDuration(int weeks) {
        this.weeks = weeks;
    }
}