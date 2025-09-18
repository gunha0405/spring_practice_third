package com.example.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {
	
	private static final String UPLOAD_ROOT = "C:\\Users\\innoa\\uploads";
	
	// 단일 파일 저장 메서드
	public static String saveFile(MultipartFile file, String folder) throws IOException{
		if(file.isEmpty()) {
			throw new IllegalArgumentException("빈 파일 저장 불가");
		}
		
		String originalName = file.getOriginalFilename();
		String savedName = UUID.randomUUID() + "_" + originalName;
		
		Path path = Paths.get(UPLOAD_ROOT, folder, savedName);
		Files.createDirectories(path.getParent());
		file.transferTo(path.toFile());
		
		return savedName;
	}
	
	// 다중 파일 저장 메서드
	public static List<String> saveFiles(List<MultipartFile> files, String folder) throws IOException{
		List<String> savedNames = new ArrayList<>();
		
		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
                String savedName = saveFile(file, folder); 
                savedNames.add(savedName);
            }
		}
		
		return savedNames;
		
	}
	
	// 브라우저에서 파일 바로보기
	public static ResponseEntity<Resource> viewFile(String folder, String savedName, String originalName) throws IOException {
		Resource resource = loadFile(folder, savedName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + originalName + "\"")
                .body(resource);
	}
	
	// 파일 다운로드
    public static ResponseEntity<Resource> downloadFile(String folder, String savedName, String originalName) throws IOException {
        Resource resource = loadFile(folder, savedName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalName + "\"")
                .body(resource);
    }
	
	private static Resource loadFile(String folder, String savedName) throws IOException {
		Path path = Paths.get(UPLOAD_ROOT, folder, savedName);
		Resource resource = new UrlResource(path.toUri());
		if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("파일을 찾을 수 없거나 읽을 수 없습니다: " + savedName);
        }
        return resource;
	}
	
	public static String getUploadRoot() {
	    return UPLOAD_ROOT;
	}
	
}
