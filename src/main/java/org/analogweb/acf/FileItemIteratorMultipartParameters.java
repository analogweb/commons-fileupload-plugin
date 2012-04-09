package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


import org.analogweb.MultipartFile;
import org.analogweb.MultipartParameters;
import org.analogweb.util.ArrayUtils;
import org.analogweb.util.IOUtils;
import org.analogweb.util.Maps;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.analogweb.util.logging.Markers;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;

/**
 * {@link FileItemIterator}を使用して、逐次的に{@link MultipartFile}を展開する
 * {@link MultipartParameters}の実装です。<br/>
 * {@link #iterator()}を使用することで、少ない消費リソースで{@link MultipartFile}
 * を読み出すことが可能です。
 * @author snowgoose
 */
public class FileItemIteratorMultipartParameters implements MultipartParameters {

    private static final Log log = Logs.getLog(FileItemIteratorMultipartParameters.class);
    private final FileItemIterator iterator;
    private final String resolvedEncoding;

    public FileItemIteratorMultipartParameters(FileItemIterator iterator, String encoding) {
        this.iterator = iterator;
        this.resolvedEncoding = encoding;
    }

    @Override
    public Iterator<MultipartParameter> iterator() {
        return new Iterator<MultipartParameters.MultipartParameter>() {

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
            public MultipartParameter next() {
                try {
                    FileItemStream stream = iterator.next();
                    final String fieldName = stream.getFieldName();
                    if (stream.isFormField()) {
                        log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000001", new Object[] { fieldName });
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        IOUtils.copy(stream.openStream(), out);
                        return new MultipartParameter() {

                            @Override
                            public boolean isMultipartFile() {
                                return false;
                            }

                            @Override
                            public String getParameterName() {
                                return fieldName;
                            }

                            @Override
                            @SuppressWarnings("unchecked")
                            public String value() {
                                try {
                                    return new String(out.toByteArray(), resolvedEncoding);
                                } catch (UnsupportedEncodingException e) {
                                    log.log(PLUGIN_MESSAGE_RESOURCE, Markers.VARIABLE_ACCESS,
                                            "WACF000001", e, fieldName);
                                    return null;
                                }
                            }

                        };
                    } else {
                        log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000002", new Object[] { fieldName });
                        final FileItemStreamMultipartFile file = new FileItemStreamMultipartFile(
                                stream);
                        return new MultipartParameter() {
                            @Override
                            public boolean isMultipartFile() {
                                return true;
                            }

                            @Override
                            public String getParameterName() {
                                return fieldName;
                            }

                            @Override
                            @SuppressWarnings("unchecked")
                            public MultipartFile value() {
                                return file;
                            }
                        };
                    }
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

    private Map<String, MultipartFile[]> files;

    @Override
    public MultipartFile[] getFile(String name) {
        return getFileMap().get(name);
    }

    @Override
    public Collection<String> getFileParameterNames() {
        return getFileMap().keySet();
    }

    @Override
    public Map<String, MultipartFile[]> getFileMap() {
        extractParameters();
        return this.files;
    }

    private void extractParameters() {
        if (this.params != null || this.files != null) {
            return;
        }
        this.params = Maps.newEmptyHashMap();
        this.files = Maps.newEmptyHashMap();
        for (MultipartParameter param : this) {
            String paramName = param.getParameterName();
            if (param.isMultipartFile()) {
                if (this.files.containsKey(paramName) == false) {
                    this.files.put(paramName, new MultipartFile[0]);
                }
                MultipartFile[] fileArray = this.files.get(paramName);
                FileItemStreamMultipartFile file = param.value();
                file.extract();
                this.files.put(paramName, ArrayUtils.add(MultipartFile.class, file, fileArray));
            } else {
                if (this.params.containsKey(paramName) == false) {
                    this.params.put(paramName, new String[0]);
                }
                String[] array = this.params.get(paramName);
                this.params.put(paramName,
                        ArrayUtils.add(String.class, (String) param.value(), array));
            }
        }
    }

}
