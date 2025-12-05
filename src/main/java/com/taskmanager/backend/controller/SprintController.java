package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.*; // Import các DTO (SprintResponse, CompleteSprintResult...)
import com.taskmanager.backend.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    // 1. Tạo Sprint
    // ✅ Sửa ResponseEntity<?> thành ResponseEntity<SprintResponse> cho rõ ràng
    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(@Valid @RequestBody CreateSprintRequest request) {
        return ResponseEntity.ok(sprintService.createSprint(request));
    }

    // 2. Start Sprint
    // ✅ Sửa thành ResponseEntity<SprintResponse>
    @PutMapping("/{id}/start")
    public ResponseEntity<SprintResponse> startSprint(@PathVariable Integer id, 
                                                      @Valid @RequestBody StartSprintRequest request) {
        return ResponseEntity.ok(sprintService.startSprint(id, request));
    }

    // 3. Edit Sprint
    // ✅ Sửa thành ResponseEntity<SprintResponse>
    @PutMapping("/{id}")
    public ResponseEntity<SprintResponse> updateSprint(@PathVariable Integer id, 
                                                       @Valid @RequestBody UpdateSprintRequest request) {
        return ResponseEntity.ok(sprintService.updateSprint(id, request));
    }

    // 4. Complete Sprint (QUAN TRỌNG NHẤT)
    // ✅ Sửa kiểu trả về thành CompleteSprintResult để FE nhận được báo cáo task di chuyển
    @PostMapping("/{id}/complete")
    public ResponseEntity<CompleteSprintResult> completeSprint(@PathVariable Integer id, 
                                                               @RequestBody CompleteSprintRequest request) {
        
        // Gọi Service và hứng kết quả trả về
        CompleteSprintResult result = sprintService.completeSprint(id, request);
        
        // Trả về Object result (chứa message, taskKeys...) thay vì String cứng
        return ResponseEntity.ok(result);
    }
}