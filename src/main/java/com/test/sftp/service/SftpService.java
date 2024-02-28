package com.test.sftp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.test.sftp.connection.SftpConnection;
import com.test.sftp.connection.SftpConnectionException;

@Service
public class SftpService {

	private static final Logger logger = LoggerFactory.getLogger(SftpService.class);

	@Value("${sftp.remoteDir}")
	private String remoteDir;

	private SftpConnection sftpConnection;
	
    @Autowired
    public SftpService(SftpConnection sftpConnection) {
        this.sftpConnection = sftpConnection;
    }

	public List<String> listFiles() throws SftpConnectionException, SftpException {
		List<String> fileNames = new ArrayList<>();
		ChannelSftp channelSftp = null;
		try {
			channelSftp = sftpConnection.openChannel();

			// Elenco dei file nella cartella remota
			List<ChannelSftp.LsEntry> list = channelSftp.ls(remoteDir);

			for (ChannelSftp.LsEntry entry : list) {
				if (!entry.getAttrs().isDir()) {
					fileNames.add(entry.getFilename());
				}
			}
		} catch (JSchException e) {
			logger.error("Error listing files: " + e.getMessage());
		} finally {
			if (channelSftp != null && channelSftp.isConnected()) {
				channelSftp.disconnect();
			}
		}

		return fileNames;
	}

	public byte[] downloadFile(String fileName) {

		byte[] fileContent = null;
		try {
			ChannelSftp channelSftp = sftpConnection.openChannel();
			// Download del file
			try (InputStream inputStream = channelSftp.get(remoteDir + fileName)) {
				fileContent = IOUtils.toByteArray(inputStream);
			}
		} catch (Exception e) {
			logger.error("Error downloading file: " + e.getMessage());
		}
		return fileContent;
	}

	public void uploadFile(String localFilePath) {

		try {
			ChannelSftp channelSftp = sftpConnection.openChannel();
			File localFile = new File(localFilePath);
			String remoteFileName = localFile.getName();

			// Upload del file
			channelSftp.put(new FileInputStream(localFile), remoteDir + remoteFileName);
		} catch (Exception e) {
			logger.error("Error uploading file: " + e.getMessage());
		}
	}
}