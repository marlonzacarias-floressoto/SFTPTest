package com.test.sftp.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.SftpException;
import com.test.sftp.connection.SftpConnectionException;
import com.test.sftp.service.SftpService;

@RestController
public class SftpController {

	private static final Logger logger = LoggerFactory.getLogger(SftpController.class);

	@Value("${sftp.file.origin}")
	private String fileOrigin;

	@Autowired
	private SftpService sftpService;

	@GetMapping("/listFiles")
	public ResponseEntity<?> listFiles() {
		try {
			List<String> fileNames = sftpService.listFiles();
			return ResponseEntity.ok(fileNames);
		} catch (SftpConnectionException | SftpException e) {
			logger.error("Error listing files: " + e.getMessage());
			return ResponseEntity.status(500).body(new ErrorResponse(500, "Error listing files: " + e.getMessage()));
		}
	}

	@GetMapping("/download/{fileName}")
	public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
		try {
			// Chiamata al metodo per il download del file
			byte[] fileContent = sftpService.downloadFile(fileName);

			// Imposta l'header Content-Disposition
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

			// Restituisci il file come risposta HTTP
			return ResponseEntity.ok().headers(headers).body(fileContent);
		} catch (Exception e) {
			logger.error("Error downloading file: " + e.getMessage());
			return ResponseEntity.status(500).body(new ErrorResponse(500, "Error downloading file: " + e.getMessage()));
		}
	}

	@PostMapping("/upload")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			return ResponseEntity.status(400).body(new ErrorResponse(400, "Invalid or empty file"));
		}

		try {
			String localFilePath = fileOrigin + file.getOriginalFilename();
			file.transferTo(new File(localFilePath));
			sftpService.uploadFile(localFilePath);
			File tempFile = new File(localFilePath);
			tempFile.delete();
			return ResponseEntity.ok("File upload completed successfully");
		} catch (IOException e) {
			logger.error("Error uploading file: " + e.getMessage());
			return ResponseEntity.status(500).body(new ErrorResponse(500, "Error uploading file: " + e.getMessage()));
		}
	}
}