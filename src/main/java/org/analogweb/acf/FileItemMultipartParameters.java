package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.analogweb.util.ArrayUtils;
import org.analogweb.util.Maps;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.apache.commons.fileupload.FileItem;

/**
 * @author snowgooseyk
 */
public class FileItemMultipartParameters implements MultipartParameters<FileItemMultipart> {

    private static final Log log = Logs.getLog(FileItemMultipartParameters.class);
    private final List<FileItemMultipart> multiparts;
    private final String encoding;
    private Map<String, String[]> params;
    private Map<String, FileItemMultipart[]> files;

    public FileItemMultipartParameters(List<FileItem> items, String encoding) {
        this.multiparts = toMultiparts(items);
        this.encoding = encoding;
    }

    protected List<FileItemMultipart> toMultiparts(List<FileItem> items) {
        final List<FileItemMultipart> multiparts = new LinkedList<FileItemMultipart>();
        for (final FileItem item : items) {
            multiparts.add(new FileItemMultipart(item));
        }
        return multiparts;
    }

    @Override
    public Iterator<FileItemMultipart> iterator() {
        return multiparts.iterator();
    }

    @Override
    public String[] getParameter(String name) {
        return getParameterMap().get(name);
    }

    @Override
    public Collection<String> getParameterNames() {
        return getParameterMap().keySet();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        extractParameters();
        return this.params;
    }

    @Override
    public FileItemMultipart[] getMultiparts(String name) {
        return asMap().get(name);
    }

    @Override
    public Collection<String> getMultipartParameterNames() {
        return asMap().keySet();
    }

    public Map<String, FileItemMultipart[]> asMap() {
        extractParameters();
        return this.files;
    }

    private void extractParameters() {
        if (this.params != null || this.files != null) {
            return;
        }
        this.params = Maps.newEmptyHashMap();
        this.files = Maps.newEmptyHashMap();
        for (final FileItemMultipart param : this) {
            final String paramName = param.getName();
            if (param.isMultipartFile()) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000002", new Object[] { paramName });
                if (this.files.containsKey(paramName) == false) {
                    this.files.put(paramName, new FileItemMultipart[0]);
                }
                final FileItemMultipart[] fileArray = this.files.get(paramName);
                this.files
                .put(paramName, ArrayUtils.add(FileItemMultipart.class, param, fileArray));
            } else {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000001", new Object[] { paramName });
                if (this.params.containsKey(paramName) == false) {
                    this.params.put(paramName, new String[0]);
                }
                final String[] array = this.params.get(paramName);
                try {
                    this.params.put(paramName, ArrayUtils.add(String.class,
                            new String(param.getBytes(), encoding), array));
                } catch (final UnsupportedEncodingException e) {
                    throw new FileUploadFailureException(e);
                }
            }
        }
    }
}
