package org.analogweb.acf;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.analogweb.InvocationMetadata;
import org.analogweb.Multipart;
import org.analogweb.RequestContext;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;

/**
 * @author snowgooseyk
 */
public class MultipartParameterStreamResolver extends MultipartParameterResolver {

    @Override
    protected Object resolveParameterizedValue(RequestContext request, InvocationMetadata metadata,
            String name, Class<?> requiredType, Annotation[] annotations,
            MultipartParameters<Multipart> parameters) {
        throw new UnsupportedParameterTypeException(name, requiredType);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T extends Multipart> MultipartParameters<T> createMultipartParameters(
            RequestContext request, org.apache.commons.fileupload.RequestContext context,
            FileUpload fileUpload, String resolvedEncoding) throws FileUploadException, IOException {
        return (MultipartParameters<T>) new FileItemStreamMultipartParameters(
                fileUpload.getItemIterator(context), resolvedEncoding);
    }
}
