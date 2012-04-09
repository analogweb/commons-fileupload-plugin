package org.analogweb.acf;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;


import org.analogweb.MultipartParameters;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;

/**
 * {@link FileItemIterator}より{@link MultipartParameters}の解決を行う
 * {@link MultipartRequestContextFactory}の拡張実装です。
 * @author snowgoose
 */
public class FileItemIteratorRequestContextFactory extends MultipartRequestContextFactory {

    @Override
    protected MultipartParameters createMultipartRequestParameters(HttpServletRequest request,
            RequestContext context, FileUpload fileUpload, String resolvedEncoding)
            throws FileUploadException {
        try {
            FileItemIterator iterator = fileUpload.getItemIterator(context);
            return new FileItemIteratorMultipartParameters(iterator, resolvedEncoding);
        } catch (IOException e) {
            throw new FileUploadException(e.getMessage(), e);
        }
    }

    @Override
    protected FileItemFactory getFileItemFactory() {
        return null;
    }

}
