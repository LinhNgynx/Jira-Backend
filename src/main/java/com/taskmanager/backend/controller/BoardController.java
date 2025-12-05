package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.BoardResponse;
import com.taskmanager.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * API: Lấy dữ liệu bảng Kanban cho một dự án cụ thể.
     * URL: GET /api/boards/project/{projectId}
     * Logic: Chỉ trả về Task trong Sprint đang ACTIVE.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Integer projectId) {
        // Gọi Service (đã bao gồm check quyền, check sprint active, gom nhóm task)
        BoardResponse response = boardService.getBoardData(projectId);
        
        return ResponseEntity.ok(response);
    }
}