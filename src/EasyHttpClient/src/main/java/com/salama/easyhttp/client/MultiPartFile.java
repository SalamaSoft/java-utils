package com.salama.easyhttp.client;

import java.io.File;
import java.io.InputStream;

/**
 * 
 * @author XingGu Liu
 *
 */
public class MultiPartFile {
	private boolean _isUseInputStream = false;
	private String _name;
	private File _file;
	private InputStream _inputStream;
	
	public boolean isUseInputStream() {
		return _isUseInputStream;
	}

	public void setUseInputStream(boolean isUseInputStream) {
		_isUseInputStream = isUseInputStream;
	}

	public InputStream getInputStream() {
		return _inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		_inputStream = inputStream;
	}

	public MultiPartFile() {
	}

	public MultiPartFile(String name, File file) {
		_name = name;
		_file = file;
	}

	public MultiPartFile(String name, InputStream inputStream) {
		_name = name;
		_inputStream = inputStream;
		_isUseInputStream = true;
	}
	
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public File getFile() {
		return _file;
	}

	public void setFile(File file) {
		_file = file;
	}
	

}
