package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.BoardResponse;
import com.taskmanager.backend.dto.MoveTaskRequest; // ✅ Import
import com.taskmanager.backend.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 1. Get Board Data
    @GetMapping("/project/{projectId}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Integer projectId) {
        BoardResponse response = boardService.getBoardData(projectId);
        return ResponseEntity.ok(response);
    }

    // 2. Move Task (Kéo thả) - ✅ API MỚI
    @PutMapping("/tasks/{taskId}/move")
    public ResponseEntity<?> moveTask(@PathVariable Integer taskId, 
                                      @Valid @RequestBody MoveTaskRequest request) {
        
        boardService.moveTask(taskId, request);
        
        // Trả về message thành công đơn giản
        return ResponseEntity.ok("Di chuyển task thành công.");
    }
}