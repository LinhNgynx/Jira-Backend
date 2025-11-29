package com.taskmanager.backend.event;

import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.enums.ActivityAction;
import com.taskmanager.backend.enums.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SystemEvent extends ApplicationEvent {
    
    // Ai làm?
    private final User actor;
    
    // Đối tượng bị tác động (Task, Project...)
    private final Object subject;
    
    // Dữ liệu cho LOG
    private final ActivityAction logAction;
    private final String logDetail; // VD: "Updated status from TO DO to DONE"

    // Dữ liệu cho NOTIFICATION (Có thể null nếu hành động này không cần báo cho ai)
    private final User recipient; 
    private final NotificationType notiType;
    private final String notiMessage; 

    // Constructor full option
    public SystemEvent(Object source, User actor, Object subject, 
                       ActivityAction logAction, String logDetail,
                       User recipient, NotificationType notiType, String notiMessage) {
        super(source);
        this.actor = actor;
        this.subject = subject;
        this.logAction = logAction;
        this.logDetail = logDetail;
        this.recipient = recipient;
        this.notiType = notiType;
        this.notiMessage = notiMessage;
    }
}