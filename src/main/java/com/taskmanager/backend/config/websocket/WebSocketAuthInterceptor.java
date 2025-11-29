package com.taskmanager.backend.config.websocket;

import com.taskmanager.backend.security.JwtUtils; // Đảm bảo import đúng package security
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ kiểm tra khi Client bắt đầu kết nối (Handshake)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            
            // 1. Lấy Token từ Header 'Authorization' của gói tin STOMP
            // Client gửi lên dạng: { Authorization: ["Bearer eyJhbGciOi..."] }
            List<String> authHeader = accessor.getNativeHeader("Authorization");
            
            if (authHeader != null && !authHeader.isEmpty()) {
                String token = authHeader.get(0).replace("Bearer ", "");
                
                try {
                    // ✅ Đã sửa tên hàm khớp với JwtUtils của bạn
                    if (jwtUtils.validateToken(token)) {
                        
                        // ✅ Đã sửa tên hàm khớp với JwtUtils của bạn
                        String email = jwtUtils.getEmailFromToken(token);
                        
                        // Load thông tin User từ DB để đảm bảo user chưa bị Ban/Xóa
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        // Tạo đối tượng Authentication
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        
                        // Gắn User vào WebSocket Session
                        // Bước này giúp hàm convertAndSendToUser hoạt động được
                        accessor.setUser(authentication);
                        
                        log.info("✅ WebSocket Authenticated User: {}", email);
                    }
                } catch (Exception e) {
                    log.error("❌ WebSocket Auth Failed: {}", e.getMessage());
                    // Không throw exception để tránh crash server, chỉ đơn giản là không set User -> Kết nối sẽ bị từ chối hoặc coi là Anonymous
                }
            }
        }
        return message;
    }
}