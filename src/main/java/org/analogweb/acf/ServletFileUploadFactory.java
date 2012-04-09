package org.analogweb.acf;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * {@link ServletFileUpload}のインスタンスを生成する{@link FileUploadFactory}の実装です。
 * @author snowgoose
 */
public class ServletFileUploadFactory implements FileUploadFactory<ServletFileUpload> {

    private long sizeMax = -1;
    private long fileSizeMax = -1;
    private String headerEncoding;
    private ProgressListener listener;

    @Override
    public ServletFileUpload createFileUpload() {
        ServletFileUpload upload = new ServletFileUpload();
        setUpFileUpload(upload);
        return upload;
    }

    @Override
    public ServletFileUpload createFileUpload(FileItemFactory fileItemFactory) {
        ServletFileUpload upload = new ServletFileUpload(fileItemFactory);
        setUpFileUpload(upload);
        return upload;
    }

    public void setUpFileUpload(FileUpload upload) {
        upload.setSizeMax(getSizeMax());
        upload.setFileSizeMax(getFileSizeMax());
        upload.setHeaderEncoding(getHeaderEncoding());
        upload.setProgressListener(getListener());
    }

    protected long getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(long sizeMax) {
        this.sizeMax = sizeMax;
    }

    protected long getFileSizeMax() {
        return fileSizeMax;
    }

    public void setFileSizeMax(long fileSizeMax) {
        this.fileSizeMax = fileSizeMax;
    }

    protected String getHeaderEncoding() {
        return headerEncoding;
    }

    public void setHeaderEncoding(String headerEncoding) {
        this.headerEncoding = headerEncoding;
    }

    protected ProgressListener getListener() {
        return listener;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

}
