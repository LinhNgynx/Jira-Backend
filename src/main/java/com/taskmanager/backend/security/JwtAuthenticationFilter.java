package com.taskmanager.backend.security;

import com.taskmanager.backend.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor; // ✅ Import cái này
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // ✅ Tự động inject dependency, không cần @Autowired
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils; // ✅ Thêm final
    private final UserDetailsServiceImpl userDetailsService; // ✅ Thêm final

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // --- ĐÃ XÓA ĐOẠN IF CHECK PATH ---

        try {
            String jwt = parseJwt(request);
            // Nếu có token và token hợp lệ
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                String email = jwtUtils.getEmailFromToken(jwt);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Lưu user vào Context (Quan trọng nhất)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}