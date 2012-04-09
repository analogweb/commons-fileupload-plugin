package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.analogweb.MultipartFile;
import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestContext;
import org.analogweb.RequestContextFactory;
import org.analogweb.core.DefaultRequestContext;
import org.analogweb.util.ArrayUtils;
import org.analogweb.util.Maps;
import org.analogweb.util.StringUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

/**
 * リクエストの形式がマルチパート(multipart/form-data)である場合に、{@link HttpServletRequest}を
 * {@link MultipartHttpServletRequest}にラップする{@link RequestContextFactory}
 * の実装です。<br/>
 * リクエストがマルチパート形式であるかは{@link ServletFileUpload#isMultipartContent(HttpServletRequest)}
 * により判別されます。
 * @author snowgoose
 */
public class MultipartRequestContextFactory implements RequestContextFactory {

    private static final Log log = Logs.getLog(MultipartRequestContextFactory.class);
    private FileItemFactory fileItemFactory = new DiskFileItemFactory();
    private FileUploadFactory<? extends FileUpload> fileUploadFactory = new ServletFileUploadFactory();
    private final String defaultEncoding = "UTF-8";

    @Override
    public RequestContext createRequestContext(ServletContext context, HttpServletRequest request,
            HttpServletResponse response) {
        if (ServletFileUpload.isMultipartContent(request)) {
            log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000001");
            FileUpload fileUpload = getFileUpload(getFileItemFactory());
            log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000002", new Object[] { fileUpload });
            String encoding = resolveEncoding(request);
            log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000003", new Object[] { encoding });
            try {
                MultipartParameters parameters = createMultipartRequestParameters(request,
                        createRequestContext(request), fileUpload, encoding);
                request = new DefaultMultipartHttpServletRequest(request, parameters);
            } catch (FileUploadException e) {
                throw new FileUploadFailureException(e);
            }
        }
        log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000004", new Object[] { request.getClass()
                .getCanonicalName() });
        return new DefaultRequestContext(request, response, context);
    }

    protected String resolveEncoding(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (StringUtils.isNotEmpty(encoding)) {
            return encoding;
        }
        return this.defaultEncoding;
    }

    @SuppressWarnings("unchecked")
    protected MultipartParameters createMultipartRequestParameters(
            HttpServletRequest request, org.apache.commons.fileupload.RequestContext context,
            FileUpload fileUpload, String resolvedEncoding) throws FileUploadException {
        List<FileItem> fileItems = fileUpload.parseRequest(context);
        log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000003", fileItems.size());
        return extractFileItems(fileItems, resolvedEncoding);
    }

    private MultipartRequestParameters extractFileItems(List<FileItem> fileItems, String encoding)
            throws FileUploadException {
        Map<String, MultipartFile[]> files = Maps.newEmptyHashMap();
        Map<String, String[]> parameters = Maps.newEmptyHashMap();
        for (FileItem fileItem : fileItems) {
            String fieldName = fileItem.getFieldName();
            if (fileItem.isFormField()) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000001", new Object[] { fieldName });
                try {
                    String param = fileItem.getString(encoding);
                    if (parameters.containsKey(fieldName)) {
                        parameters.put(fieldName,
                                ArrayUtils.add(String.class, param, parameters.get(fieldName)));
                    } else {
                        parameters.put(fieldName, ArrayUtils.newArray(param));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new FileUploadException(e.getMessage());
                }
            } else {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000002", new Object[] { fieldName });
                if (files.containsKey(fieldName)) {
                    files.put(fieldName, ArrayUtils.add(MultipartFile.class,
                            new FileItemMultipartFile(fileItem), files.get(fieldName)));
                } else {
                    files.put(fieldName, ArrayUtils.newArray(new FileItemMultipartFile(fileItem)));
                }
            }
        }
        return new MultipartRequestParameters(parameters, files);
    }

    protected org.apache.commons.fileupload.RequestContext createRequestContext(
            HttpServletRequest request) {
        return new ServletRequestContext(request);
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

    public void setFileItemFactory(FileItemFactory fileItemFactory) {
        this.fileItemFactory = fileItemFactory;
    }

    public void setFileUploadFactory(FileUploadFactory<? extends FileUpload> fileUploadFactory) {
        this.fileUploadFactory = fileUploadFactory;
    }

}
