package org.example.controller;

import org.example.dto.AnalysisRequest;
import org.example.dto.FileComparisonResult;
import org.example.service.FileAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private FileAnalysisService analysisService;

    private final Map<String, List<FileComparisonResult>> taskResults = new ConcurrentHashMap<>();
    private final Map<String, Boolean> taskStatus = new ConcurrentHashMap<>();

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyze(@RequestBody AnalysisRequest request) {
        if (request.getOriginalDir() == null || request.getOriginalDir().trim().isEmpty() ||
                request.getDamagedDir() == null || request.getDamagedDir().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Both directories must be specified"));
        }

        String taskId = UUID.randomUUID().toString();

        taskStatus.put(taskId, false);

        new Thread(() -> {
            try {
                List<FileComparisonResult> results = analysisService.analyzeDirectories(
                        request.getOriginalDir(),
                        request.getDamagedDir()
                );
                taskResults.put(taskId, results);
                taskStatus.put(taskId, true); // Отмечаем как завершенную
            } catch (Exception e) {
                e.printStackTrace();
                taskStatus.put(taskId, true); // Даже при ошибке отмечаем как завершенную
            }
        }).start();

        return ResponseEntity.ok(Map.of("taskId", taskId));
    }

    @GetMapping("/results/{taskId}")
    public ResponseEntity<?> getResults(@PathVariable String taskId) {

        if (!taskStatus.containsKey(taskId)) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Task not found"));
        }


        if (!taskStatus.get(taskId)) {
            return ResponseEntity.ok(List.of()); // Пустой список означает "еще в процессе"
        }


        List<FileComparisonResult> results = taskResults.get(taskId);
        if (results == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Results not found for task"));
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "service", "File Damage Analyzer"));
    }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getTasks() {
        Map<String, Object> response = Map.of(
                "totalTasks", taskStatus.size(),
                "completedTasks", taskStatus.values().stream().filter(status -> status).count(),
                "activeTasks", taskStatus.values().stream().filter(status -> !status).count()
        );
        return ResponseEntity.ok(response);
    }
}