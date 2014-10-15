package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.analogweb.util.ArrayUtils;
import org.analogweb.util.Maps;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;

/**
 * {@link FileItemIterator}を使用して、逐次的に{@link org.analogweb.Multipart}を展開する
 * {@link MultipartParameters}の実装です。<br/>
 * {@link #iterator()}を使用することで、少ない消費リソースで{@link org.analogweb.Multipart}
 * を読み出すことが可能です。
 * @author snowgoose
 */
public class FileItemStreamMultipartParameters implements MultipartParameters<FileItemStreamMultipart> {

    private static final Log log = Logs.getLog(FileItemStreamMultipartParameters.class);
    private final FileItemIterator iterator;
    private final String resolvedEncoding;

    public FileItemStreamMultipartParameters(FileItemIterator iterator, String encoding) {
        this.iterator = iterator;
        this.resolvedEncoding = encoding;
    }

    public Iterator<FileItemStreamMultipart> iterator() {
        return new Iterator<FileItemStreamMultipart>() {

            @Override
            public boolean hasNext() {
                try {
                    return iterator.hasNext();
                } catch (FileUploadException e) {
                    throw new FileUploadFailureException(e);
                } catch (IOException e) {
                    throw new FileUploadFailureException(e);
                }
            }

            @Override
            public FileItemStreamMultipart next() {
                try {
                    final FileItemStream stream = iterator.next();
                    return new FileItemStreamMultipart(stream);
                } catch (IOException e) {
                    throw new FileUploadFailureException(e);
                } catch (FileUploadException e) {
                    throw new FileUploadFailureException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Map<String, String[]> params;

    public String[] getParameter(String name) {
        return getParameterMap().get(name);
    }

    public Collection<String> getParameterNames() {
        return getParameterMap().keySet();
    }

    public Map<String, String[]> getParameterMap() {
        extractParameters();
        return this.params;
    }

    private Map<String, FileItemStreamMultipart[]> files;

    public FileItemStreamMultipart[] getMultiparts(String name) {
        return asMap().get(name);
    }

    public Collection<String> getMultipartParameterNames() {
        return asMap().keySet();
    }

    public Map<String, FileItemStreamMultipart[]> asMap() {
        extractParameters();
        return this.files;
    }

    private void extractParameters() {
        if (this.params != null || this.files != null) {
            return;
        }
        this.params = Maps.newEmptyHashMap();
        this.files = Maps.newEmptyHashMap();
        for (FileItemStreamMultipart param : this) {
            String paramName = param.getName();
            if (param.isMultipartFile()) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000002", new Object[] { paramName });
                if (this.files.containsKey(paramName) == false) {
                    this.files.put(paramName, new FileItemStreamMultipart[0]);
                }
                FileItemStreamMultipart[] fileArray = this.files.get(paramName);
                this.files.put(paramName,
                        ArrayUtils.add(FileItemStreamMultipart.class, param, fileArray));
            } else {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000001", new Object[] { paramName });
                if (this.params.containsKey(paramName) == false) {
                    this.params.put(paramName, new String[0]);
                }
                String[] array = this.params.get(paramName);
                try {
                    this.params.put(paramName, ArrayUtils.add(String.class,
                            new String(param.getBytes(), resolvedEncoding), array));
                } catch (UnsupportedEncodingException e) {
                    throw new FileUploadFailureException(e);
                }
            }
        }
    }

}
