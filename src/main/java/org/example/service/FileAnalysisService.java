package org.example.service;

import org.springframework.stereotype.Service;

@Service
public class FileAnalysisService {
    public void analyzeDirectories(String originalDir, String damagedDir) {
        System.out.println("Analyzing: " + originalDir + " vs " + damagedDir);
    }
}
