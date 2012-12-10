package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.InvocationMetadata;
import org.analogweb.Multipart;
import org.analogweb.RequestContext;
import org.analogweb.ServletRequestContext;
import org.analogweb.core.ParameterScopeRequestAttributesResolver;
import org.analogweb.util.ArrayUtils;
import org.analogweb.util.StringUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class MultipartParameterResolver extends ParameterScopeRequestAttributesResolver {

    private static final Log log = Logs.getLog(MultipartParameterResolver.class);
    private FileItemFactory fileItemFactory;
    private FileUploadFactory<? extends FileUpload> fileUploadFactory = new ServletFileUploadFactory();
    private final String defaultEncoding = "UTF-8";
    static final String KEY_IS_MULTIPART_CONTENT = MultipartParameterResolver.class
            .getCanonicalName() + ".IS_MULTIPART_CONTENT";

    @Override
    public Object resolveAttributeValue(RequestContext requestContext, InvocationMetadata metadata,
            String name, Class<?> requiredType) {
        if (requestContext instanceof ServletRequestContext) {
            HttpServletRequest request = ((ServletRequestContext) requestContext)
                    .getServletRequest();
            MultipartParameters<Multipart> parameters = CurrentMultipartParameters
                    .get(request);
            if (parameters == null) {
                if (isMultipartContentOnCurrentRequest(request)) {
                    log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000001");
                    FileUpload fileUpload = getFileUpload(getFileItemFactory());
                    log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000002", new Object[] { fileUpload });
                    String encoding = resolveEncoding(request);
                    log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000003", new Object[] { encoding });
                    try {
                        parameters = createMultipartParameters(request,
                                createRequestContext(request), fileUpload, encoding);
                        CurrentMultipartParameters.put(request, parameters);
                    } catch (FileUploadException e) {
                        throw new FileUploadFailureException(e);
                    } catch (IOException e) {
                        throw new FileUploadFailureException(e);
                    }
                } else {
                    return super
                            .resolveAttributeValue(requestContext, metadata, name, requiredType);
                }
            }
            if (isEqualsType(Iterable.class, requiredType)) {
                return parameters;
            }
            Multipart[] value = parameters.getMultiparts(name);
            if (ArrayUtils.isNotEmpty(value)) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000004", new Object[] { name, value });
                if (isEqualsType(File[].class, requiredType)) {
                    List<File> files = new ArrayList<File>();
                    for (Multipart mp : value) {
                        files.add(TemporaryUploadFolder.require(requestContext, mp));
                    }
                    return files.toArray(new File[files.size()]);
                } else if (isEqualsType(Multipart[].class, requiredType)) {
                    return value;
                }
                Multipart mp = value[0];
                if (isEqualsType(InputStream.class, requiredType)) {
                    return mp.getInputStream();
                } else if (isEqualsType(File.class, requiredType)) {
                    return TemporaryUploadFolder.require(requestContext, mp);
                } else if (isEqualsType(byte[].class, requiredType)) {
                    return mp.getBytes();
                } else if (isEqualsType(Multipart.class, requiredType)) {
                    return mp;
                } else {
                    throw new UnsupportedParameterTypeException(name, requiredType);
                }
            }
        }
        return super.resolveAttributeValue(requestContext, metadata, name, requiredType);
    }

    protected boolean isMultipartContentOnCurrentRequest(HttpServletRequest request) {
        Object value = request.getAttribute(KEY_IS_MULTIPART_CONTENT);
        Boolean isMultipartContentOnCurrentRequest;
        if (value instanceof Boolean) {
            isMultipartContentOnCurrentRequest = (Boolean) value;
        } else {
            isMultipartContentOnCurrentRequest = ServletFileUpload.isMultipartContent(request);
            request.setAttribute(KEY_IS_MULTIPART_CONTENT, isMultipartContentOnCurrentRequest);
        }
        return isMultipartContentOnCurrentRequest;
    }

    protected String resolveEncoding(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (StringUtils.isNotEmpty(encoding)) {
            return encoding;
        }
        return this.defaultEncoding;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Multipart> MultipartParameters<T> createMultipartParameters(
            HttpServletRequest request, org.apache.commons.fileupload.RequestContext context,
            FileUpload fileUpload, String resolvedEncoding) throws FileUploadException, IOException {
        List<FileItem> fileItems = fileUpload.parseRequest(context);
        log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000003", fileItems.size());
        FileItemIterator iterator = fileUpload.getItemIterator(context);
        return (MultipartParameters<T>) new FileItemIteratorMultipartParameters(iterator, resolvedEncoding);
    }

    protected org.apache.commons.fileupload.RequestContext createRequestContext(
            HttpServletRequest request) {
        return new org.apache.commons.fileupload.servlet.ServletRequestContext(request);
    }

    protected FileItemFactory getFileItemFactory() {
        return this.fileItemFactory;
    }

    protected FileUpload getFileUpload(FileItemFactory fileItemFactory) {
        if (fileItemFactory != null) {
            return fileUploadFactory.createFileUpload(fileItemFactory);
        } else {
            return fileUploadFactory.createFileUpload();
        }
    }

    private boolean isEqualsType(Class<?> clazz, Class<?> clazz2) {
        return (clazz == clazz2) || clazz.getCanonicalName().equals(clazz2.getCanonicalName());
    }
}
