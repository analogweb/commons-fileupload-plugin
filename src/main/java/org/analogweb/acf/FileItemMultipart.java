package org.analogweb.acf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.analogweb.Multipart;
import org.analogweb.core.ApplicationRuntimeException;
import org.analogweb.util.IOUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;

public class FileItemMultipart implements Multipart {

    private static final Log log = Logs.getLog(FileItemMultipart.class);
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
            final File f = d.getStoreLocation();
            if (f != null) {
                return f;
            } else {
                try {
                    // Force output to file.
                    IOUtils.copy(d.getInputStream(), d.getOutputStream());
                    return d.getStoreLocation();
                } catch (IOException e) {
                    log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "WACF000002",
                            e, d.getName());
                }
            }
        }
        return null;
    }
}
