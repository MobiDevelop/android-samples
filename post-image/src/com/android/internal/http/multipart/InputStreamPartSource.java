package com.android.internal.http.multipart;

import java.io.InputStream;
import java.io.IOException;

public class InputStreamPartSource implements PartSource
{
	private String fileName;
	private InputStream inputStream;
	
	public InputStreamPartSource(String fileName, InputStream inputStream) {
		this.fileName = fileName;
		this.inputStream = inputStream;
	}
	public long getLength() {
		long length = 0;
		if (inputStream != null) {
			try {
				length = inputStream.available();
			}
			catch (IOException ignored) {
			}
		}
		return length;
	}

	public String getFileName() {
		return fileName;
	}

	public InputStream createInputStream() throws IOException {
		return inputStream;
	}
}
