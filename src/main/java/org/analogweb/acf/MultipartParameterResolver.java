package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.analogweb.InvocationMetadata;
import org.analogweb.MediaType;
import org.analogweb.Multipart;
import org.analogweb.RequestContext;
import org.analogweb.core.MediaTypes;
import org.analogweb.core.ParameterValueResolver;
import org.analogweb.core.SpecificMediaTypeRequestValueResolver;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.analogweb.util.ArrayUtils;
import org.analogweb.util.ClassUtils;
import org.analogweb.util.StringUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileCleaningTracker;

public class MultipartParameterResolver extends ParameterValueResolver implements SpecificMediaTypeRequestValueResolver {

    private static final Log log = Logs.getLog(MultipartParameterResolver.class);
    private FileItemFactory fileItemFactory = createFileItemFactory();
    private FileUploadFactory<? extends FileUpload> fileUploadFactory = new DefaultFileUploadFactory();
    private final String defaultEncoding = "UTF-8";

    @Override
    public boolean supports(MediaType mediaType) {
        return MediaTypes.MULTIPART_FORM_DATA_TYPE.isCompatible(mediaType);
    }

    @Override
    public Object resolveValue(RequestContext request, InvocationMetadata metadata, String name,
            Class<?> requiredType, Annotation[] annotations) {
        MultipartParameters<Multipart> parameters = CurrentMultipartParameters.get(request);
        if (parameters == null) {
            if (isMultipartContentOnCurrentRequest(request)) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000001");
                final FileUpload fileUpload = getFileUpload(getFileItemFactory());
                log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000002", new Object[] { fileUpload });
                final String encoding = resolveEncoding(request);
                log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000003", new Object[] { encoding });
                try {
                    parameters = createMultipartParameters(request, createRequestContext(request),
                            fileUpload, encoding);
                    CurrentMultipartParameters.put(request, parameters);
                } catch (final FileUploadException e) {
                    throw new FileUploadFailureException(e);
                } catch (final IOException e) {
                    throw new FileUploadFailureException(e);
                }
            } else {
                log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000009",
                        new Object[] { request.getContentType() });
                return super.resolveValue(request, metadata, name, requiredType, annotations);
            }
        }
        log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000009", new Object[] { parameters, name,
                requiredType });
        if (isEqualsType(Iterable.class, requiredType)) {
            return parameters;
        }
        return resolveParameterizedValue(request, metadata, name, requiredType, annotations,
                parameters);
    }

    protected Object resolveParameterizedValue(RequestContext request, InvocationMetadata metadata,
            String name, Class<?> requiredType, Annotation[] annotations,
            MultipartParameters<Multipart> parameters) {
        final Multipart[] value = parameters.getMultiparts(name);
        if (ArrayUtils.isNotEmpty(value)) {
            log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000004", new Object[] { name, value });
            if (isEqualsType(ClassUtils.forNameQuietly("[L" + File.class.getName() + ";"),
                    requiredType)) {
                final List<File> files = new ArrayList<File>();
                for (final Multipart mp : value) {
                    File f;
                    if (mp instanceof FileItemMultipart
                            && (f = ((FileItemMultipart) mp).getAsTemporalyFile()) != null) {
                        files.add(f);
                    }
                }
                return files.toArray(new File[files.size()]);
            } else if (isEqualsType(
                    ClassUtils.forNameQuietly("[L" + Multipart.class.getName() + ";"), requiredType)) {
                return value;
            }
            final Multipart mp = value[0];
            if (isEqualsType(InputStream.class, requiredType)) {
                return mp.getInputStream();
            } else if (isEqualsType(File.class, requiredType)) {
                File f;
                if (mp instanceof FileItemMultipart
                        && (f = ((FileItemMultipart) mp).getAsTemporalyFile()) != null) {
                    return f;
                }
            } else if (isEqualsType(byte[].class, requiredType)) {
                return mp.getBytes();
            } else if (isEqualsType(Multipart.class, requiredType)) {
                return mp;
            } else {
                throw new UnsupportedParameterTypeException(name, requiredType);
            }
        }
        return super.resolveValue(request, metadata, name, requiredType, annotations);
    }

    protected boolean isMultipartContentOnCurrentRequest(RequestContext request) {
        final String method = request.getRequestMethod();
        if (StringUtils.isEmpty(method) || method.equalsIgnoreCase("POST") == false) {
            return false;
        }
        final MediaType type = request.getContentType();
        if (type == null) {
            return false;
        }
        return type.toString().startsWith("multipart/");
    }

    protected String resolveEncoding(RequestContext request) {
        final String encoding = request.getCharacterEncoding();
        if (StringUtils.isNotEmpty(encoding)) {
            return encoding;
        }
        return this.defaultEncoding;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Multipart> MultipartParameters<T> createMultipartParameters(
            RequestContext request, org.apache.commons.fileupload.RequestContext context,
            FileUpload fileUpload, String resolvedEncoding) throws FileUploadException, IOException {
        final List<FileItem> fileItems = fileUpload.parseRequest(context);
        log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000003", fileItems.size());
        return (MultipartParameters<T>) new FileItemMultipartParameters(fileItems, resolvedEncoding);
    }

    protected org.apache.commons.fileupload.RequestContext createRequestContext(
            RequestContext request) {
        return new AnalogwebRequestContext(request);
    }

    protected FileItemFactory getFileItemFactory() {
        return this.fileItemFactory;
    }

    protected FileItemFactory createFileItemFactory() {
        File tmpDir = ApplicationPropertiesHolder.current()
                .getTempDir();
        if (tmpDir.exists() == false) {
            try {
                boolean created = tmpDir.mkdirs();
                if (created == false) {
                    log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "WACF000003",
                            tmpDir.getPath());
                }
            } catch (SecurityException e) {
                log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "WACF000003", e,
                        tmpDir.getPath());
            }
        }
        final DiskFileItemFactory factory = new DiskFileItemFactory(
                DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, tmpDir);
        factory.setFileCleaningTracker(new FileCleaningTracker());
        return factory;
    }

    protected FileUpload getFileUpload(FileItemFactory fileItemFactory) {
        if (fileItemFactory != null) {
            return fileUploadFactory.createFileUpload(fileItemFactory);
        } else {
            return fileUploadFactory.createFileUpload();
        }
    }

    private boolean isEqualsType(Class<?> clazz, Class<?> other) {
        if (clazz == null || other == null) {
            return false;
        }
        return (clazz == other) || clazz.getCanonicalName().equals(other.getCanonicalName());
    }

    public void setFileItemFactory(FileItemFactory fileItemFactory) {
        this.fileItemFactory = fileItemFactory;
    }

    public void setFileUploadFactory(FileUploadFactory<? extends FileUpload> fileUploadFactory) {
        this.fileUploadFactory = fileUploadFactory;
    }
}
