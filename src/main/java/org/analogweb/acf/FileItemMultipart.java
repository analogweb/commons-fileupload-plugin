package org.analogweb.acf;

import java.io.IOException;
import java.io.InputStream;

import org.analogweb.Multipart;
import org.analogweb.core.ApplicationRuntimeException;
import org.apache.commons.fileupload.FileItem;

public class FileItemMultipart implements Multipart {

	private final FileItem item;

	public FileItemMultipart(FileItem item) {
		this.item = item;
	}

	public boolean isMultipartFile(){
		return item.isFormField() == false;
	}
	@Override
	public String getName() {
		return item.getFieldName();
	}

	@Override
	public String getResourceName() {
		return item.getName();
	}

	@Override
	public InputStream getInputStream() {
		try {
			return item.getInputStream();
		} catch (IOException e) {
			throw new ApplicationRuntimeException(e) {
				private static final long serialVersionUID = 1L;
			};
		}
	}

	@Override
	public byte[] getBytes() {
		return item.get();
	}

	@Override
	public String getContentType() {
		return item.getContentType();
	}

}
