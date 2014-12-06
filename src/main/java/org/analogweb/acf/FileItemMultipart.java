package org.analogweb.acf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.analogweb.Multipart;
import org.analogweb.core.ApplicationRuntimeException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;

public class FileItemMultipart implements Multipart {

    private final FileItem item;

    public FileItemMultipart(FileItem item) {
        this.item = item;
    }

    public boolean isMultipartFile() {
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
        } catch (final IOException e) {
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

    public File getAsTemporalyFile() {
        if (item instanceof DiskFileItem) {
            final DiskFileItem d = ((DiskFileItem) item);
            if (d.isInMemory()) {
                final File f = d.getStoreLocation();
                try {
                    d.write(f);
                    return f;
                } catch (final Exception e) {
                    return null;
                }
            } else {
                return d.getStoreLocation();
            }
        }
        return null;
    }
}
