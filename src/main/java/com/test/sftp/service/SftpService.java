package com.test.sftp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.test.sftp.controller.SftpController;

@Service
public class SftpService {
	
	private static final Logger logger = LoggerFactory.getLogger(SftpController.class);

	@Value("${sftp.host}")
	private String host;

	@Value("${sftp.port}")
	private int port;

	@Value("${sftp.user}")
	private String user;

	@Value("${sftp.password}")
	private String password;

	@Value("${sftp.remoteDir}")
	private String remoteDir;

	@Value("${sftp.file.destination}")
	private String fileDest;

	public List<String> listFiles() {
		JSch jsch = new JSch();
		Session session = null;
		List<String> fileNames = new ArrayList<>();

		try {
			session = jsch.getSession(user, host, port);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();

			ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();

			// Elenco dei file nella cartella remota
			List<ChannelSftp.LsEntry> list = channelSftp.ls(remoteDir);

			for (ChannelSftp.LsEntry entry : list) {
				if (!entry.getAttrs().isDir()) {
					fileNames.add(entry.getFilename());
				}
			}

			channelSftp.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileNames;
	}

	public byte[] downloadFile(String fileName) {
		byte[] fileContent = null;

		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(user, host, port);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();

			ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();

			// Download del file
			InputStream inputStream = channelSftp.get(remoteDir + fileName);
			fileContent = IOUtils.toByteArray(inputStream);

			channelSftp.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileContent;
	}

	public void uploadFile(String localFilePath) {
		JSch jsch = new JSch();
		Session session = null;

		try {
			session = jsch.getSession(user, host, port);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();

			ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();

			File localFile = new File(localFilePath);
			String remoteFileName = localFile.getName();

			// Upload del file
			channelSftp.put(new FileInputStream(localFile), remoteDir + remoteFileName);

			channelSftp.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}