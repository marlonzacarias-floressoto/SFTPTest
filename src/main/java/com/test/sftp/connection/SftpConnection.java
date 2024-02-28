package com.test.sftp.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class SftpConnection {

	private static final Logger logger = LoggerFactory.getLogger(SftpConnection.class);

	@Value("${sftp.host}")
	private String host;

	@Value("${sftp.port}")
	private int port;

	@Value("${sftp.user}")
	private String user;

	@Value("${sftp.password}")
	private String password;

	private Session session;

	public void connect() throws SftpConnectionException {
		JSch jsch = new JSch();

		try {
			session = jsch.getSession(user, host, port);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();
			logger.info("Connection established...");

		} catch (JSchException e) {
			logger.info("Error connecting to SFTP server: " + e.getMessage());
			throw new SftpConnectionException("Error connecting to SFTP server", e);
		}
	}

	public ChannelSftp openChannel() throws JSchException, SftpConnectionException {
        try {
            if (session == null || !session.isConnected()) {
                connect();
            }

            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            logger.info("Channel opened...");
            return channelSftp;
        } catch (JSchException e) {
            logger.error("Error opening SFTP channel: " + e.getMessage());
            throw new SftpConnectionException("Error opening SFTP channel", e);
        }
	}

	public void disconnect() {
		if (session != null && session.isConnected()) {
			session.disconnect();
			logger.info("Disconnected...");
		}
	}
}
