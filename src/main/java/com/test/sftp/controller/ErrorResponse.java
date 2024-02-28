package com.test.sftp.controller;

public class ErrorResponse {

	private int statusCode;
	private String message;

	public ErrorResponse(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}

	private int getStatusCode() {
		return statusCode;
	}

	private String getMessage() {
		return message;
	}
}
