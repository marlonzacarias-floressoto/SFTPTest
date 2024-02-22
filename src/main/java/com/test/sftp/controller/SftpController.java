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

import com.test.sftp.service.SftpService;

@RestController
public class SftpController {
	
	private static final Logger logger = LoggerFactory.getLogger(SftpController.class);

	@Value("${sftp.file.destination}")
	private String fileDest;
	
	@Autowired
	private SftpService sftpService;

	@GetMapping("/listFiles")
	public List<String> listFiles() {
		return sftpService.listFiles();
	}

	@GetMapping("/download/{fileName}")
	public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
		try {
			// Chiamata al metodo per il download del file
			byte[] fileContent = sftpService.downloadFile(fileName);

			// Imposta l'header Content-Disposition
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

			// Restituisci il file come risposta HTTP
			return ResponseEntity.ok().headers(headers).body(fileContent);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(null);
		}
	}

	@PostMapping("/upload")
	public String uploadFile(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			return "File non valido o vuoto";
		}

		try {
			// Salva il file temporaneo sul disco
			String localFilePath = fileDest + file.getOriginalFilename();
			file.transferTo(new File(localFilePath));

			// Chiamata al metodo per l'upload del file
			sftpService.uploadFile(localFilePath);

			// Elimina il file temporaneo dopo l'upload
			File tempFile = new File(localFilePath);
			tempFile.delete();

			return "Upload del file completato con successo";
		} catch (IOException e) {
			e.printStackTrace();
			return "Errore durante l'upload del file";
		}
	}
}