package com.taskmanager.backend.event;

import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.enums.ActivityAction;
import com.taskmanager.backend.enums.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SystemEvent extends ApplicationEvent {
    
    private final User actor;
    private final Object subject;
    
    private final ActivityAction logAction;
    private final String logDetail; // Dùng cho mô tả chung
    
    // ✅ THÊM MỚI: Dữ liệu chi tiết thay đổi
    private final String oldValue;
    private final String newValue;

    private final User recipient; 
    private final NotificationType notiType;
    private final String notiMessage; 

    // Constructor cập nhật
    public SystemEvent(Object source, User actor, Object subject, 
                       ActivityAction logAction, String logDetail, 
                       String oldValue, String newValue, // Nhận thêm tham số
                       User recipient, NotificationType notiType, String notiMessage) {
        super(source);
        this.actor = actor;
        this.subject = subject;
        this.logAction = logAction;
        this.logDetail = logDetail;
        this.oldValue = oldValue; // Gán
        this.newValue = newValue; // Gán
        this.recipient = recipient;
        this.notiType = notiType;
        this.notiMessage = notiMessage;
    }
}