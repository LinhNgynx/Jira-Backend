package com.taskmanager.backend.enums;

public enum IssueLevel {
    EPIC,       // Cấp 0: Cha to nhất (Dự án lớn)
    STANDARD,   // Cấp 1: Việc thường (Story, Task, Bug)
    SUBTASK     // Cấp 2: Việc vặt (Con của Cấp 1)
}