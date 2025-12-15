package org.example.service;

import org.example.dto.DamageDetail;
import org.example.dto.FileComparisonResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileAnalysisService {

    public List<FileComparisonResult> analyzeDirectories(String originalDirPath, String damagedDirPath) {
        List<FileComparisonResult> results = new ArrayList<>();

        try {
            Path originalDir = Paths.get(originalDirPath).toAbsolutePath();
            Path damagedDir = Paths.get(damagedDirPath).toAbsolutePath();


            if (!Files.exists(originalDir) || !Files.isDirectory(originalDir)) {
                throw new IllegalArgumentException("Original directory does not exist: " + originalDir);
            }

            if (!Files.exists(damagedDir) || !Files.isDirectory(damagedDir)) {
                throw new IllegalArgumentException("Damaged directory does not exist: " + damagedDir);
            }


            try (DirectoryStream<Path> stream = Files.newDirectoryStream(originalDir)) {
                for (Path originalFile : stream) {
                    if (Files.isRegularFile(originalFile)) {
                        FileComparisonResult result = analyzeFile(originalFile, damagedDir);
                        results.add(result);
                    }
                }
            }


            try (DirectoryStream<Path> stream = Files.newDirectoryStream(damagedDir)) {
                for (Path damagedFile : stream) {
                    if (Files.isRegularFile(damagedFile)) {
                        Path originalFile = originalDir.resolve(damagedFile.getFileName());
                        if (!Files.exists(originalFile)) {

                            FileComparisonResult result = new FileComparisonResult();
                            result.setFilename(damagedFile.getFileName().toString());
                            result.setDamaged(true);
                            result.setDamages(List.of(
                                    createDamageDetail("File exists only in damaged directory", "File missing in original")
                            ));
                            results.add(result);
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error analyzing directories: " + e.getMessage(), e);
        }

        return results;
    }

    private FileComparisonResult analyzeFile(Path originalFile, Path damagedDir) throws IOException {
        FileComparisonResult result = new FileComparisonResult();
        result.setFilename(originalFile.getFileName().toString());

        Path damagedFile = damagedDir.resolve(originalFile.getFileName());


        if (!Files.exists(damagedFile) || !Files.isRegularFile(damagedFile)) {
            result.setDamaged(true);
            result.setDamages(List.of(
                    createDamageDetail("File exists", "File missing")
            ));
            return result;
        }


        long originalSize = Files.size(originalFile);
        long damagedSize = Files.size(damagedFile);

        if (originalSize != damagedSize) {
            result.setDamaged(true);
            result.setDamages(List.of(
                    createDamageDetail(
                            String.format("Size: %d bytes", originalSize),
                            String.format("Size: %d bytes", damagedSize)
                    )
            ));
            return result;
        }


        List<DamageDetail> damages = compareFileContents(originalFile, damagedFile);

        result.setDamaged(!damages.isEmpty());
        result.setDamages(damages);

        return result;
    }

    private List<DamageDetail> compareFileContents(Path originalFile, Path damagedFile) throws IOException {
        List<DamageDetail> damages = new ArrayList<>();

        try (InputStream originalStream = Files.newInputStream(originalFile);
             InputStream damagedStream = Files.newInputStream(damagedFile)) {

            byte[] originalBuffer = new byte[8192];
            byte[] damagedBuffer = new byte[8192];

            long offset = 0;
            int bytesReadOriginal;

            while ((bytesReadOriginal = originalStream.read(originalBuffer)) != -1) {
                int bytesReadDamaged = damagedStream.read(damagedBuffer, 0, bytesReadOriginal);


                if (bytesReadDamaged != bytesReadOriginal) {
                    damages.add(createDamageDetail(
                            String.format("Bytes at offset %d: %d", offset, bytesReadOriginal),
                            String.format("Bytes at offset %d: %d", offset, bytesReadDamaged)
                    ));
                    break;
                }


                for (int i = 0; i < bytesReadOriginal; i++) {
                    if (originalBuffer[i] != damagedBuffer[i]) {
                        DamageDetail detail = new DamageDetail();
                        detail.setOffset(offset + i);
                        detail.setOriginalByte(originalBuffer[i] & 0xFF); // Конвертируем в unsigned
                        detail.setDamagedByte(damagedBuffer[i] & 0xFF);
                        detail.setHexOriginal(String.format("%02X", originalBuffer[i] & 0xFF));
                        detail.setHexDamaged(String.format("%02X", damagedBuffer[i] & 0xFF));
                        damages.add(detail);


                        if (damages.size() >= 100) {
                            damages.add(createDamageDetail(
                                    "Further analysis stopped",
                                    "Too many differences found (limit: 100)"
                            ));
                            return damages;
                        }
                    }
                }

                offset += bytesReadOriginal;
            }
        }

        return damages;
    }

    private DamageDetail createDamageDetail(String originalInfo, String damagedInfo) {
        DamageDetail detail = new DamageDetail();
        detail.setOffset(0);
        detail.setOriginalByte(0);
        detail.setDamagedByte(0);
        detail.setHexOriginal(originalInfo);
        detail.setHexDamaged(damagedInfo);
        return detail;
    }
}