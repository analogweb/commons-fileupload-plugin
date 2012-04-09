package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.analogweb.MultipartParameters;
import org.analogweb.acf.DefaultMultipartHttpServletRequest;
import org.analogweb.acf.FileItemMultipartFile;
import org.analogweb.acf.FileUploadFactory;
import org.analogweb.acf.FileUploadFailureException;
import org.analogweb.acf.MultipartRequestContextFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.RequestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MultipartRequestContextFactoryTest {

    private MultipartRequestContextFactory contextFactory;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private FileItemFactory fileItemFactory;
    private FileUploadFactory<FileUpload> fileUploadFactory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * @throws java.lang.Exception
     */
    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        servletContext = mock(ServletContext.class);
        fileItemFactory = mock(FileItemFactory.class);
        fileUploadFactory = mock(FileUploadFactory.class);
    }

    @Test
    public void testIsMultipartRequest() throws Exception {
        schenarioIsMultipartRequest("UTF-8");
    }

    @Test
    public void testIsMultipartRequest2() throws Exception {
        when(request.getCharacterEncoding()).thenReturn("Shift-JIS");
        schenarioIsMultipartRequest("Shift-JIS");
    }

    private void schenarioIsMultipartRequest(String charset) throws Exception {

        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("multipart/form-data");

        final FileItem fileItem = mock(FileItem.class);
        final FileItem fileItem2 = mock(FileItem.class);
        final FileItem fileItem3 = mock(FileItem.class);
        final FileItem paramItem = mock(FileItem.class);
        final FileItem paramItem2 = mock(FileItem.class);
        final FileItem paramItem3 = mock(FileItem.class);
        // requested parameters.
        when(fileItem.isFormField()).thenReturn(false);
        when(fileItem.getFieldName()).thenReturn("foo");
        when(fileItem2.isFormField()).thenReturn(false);
        when(fileItem2.getFieldName()).thenReturn("baa");
        when(fileItem3.isFormField()).thenReturn(false);
        when(fileItem3.getFieldName()).thenReturn("baa");
        when(paramItem.isFormField()).thenReturn(true);
        when(paramItem.getFieldName()).thenReturn("baa");
        when(paramItem.getString(charset)).thenReturn("ばー");
        when(paramItem2.isFormField()).thenReturn(true);
        when(paramItem2.getFieldName()).thenReturn("baz");
        when(paramItem2.getString(charset)).thenReturn("ばず");
        when(paramItem3.isFormField()).thenReturn(true);
        when(paramItem3.getFieldName()).thenReturn("baa");
        when(paramItem3.getString(charset)).thenReturn("ばぁあ");

        when(fileUploadFactory.createFileUpload(fileItemFactory)).thenReturn(new FileUpload() {
            @Override
            @SuppressWarnings("rawtypes")
            public List parseRequest(RequestContext context) {
                return Arrays.asList(fileItem, fileItem2, fileItem3, paramItem, paramItem2,
                        paramItem3);
            }
        });

        contextFactory = new MultipartRequestContextFactory();
        contextFactory.setFileItemFactory(fileItemFactory);
        contextFactory.setFileUploadFactory(fileUploadFactory);

        HttpServletRequest actual = contextFactory.createRequestContext(servletContext, request,
                response).getRequest();
        assertThat(actual, is(instanceOf(DefaultMultipartHttpServletRequest.class)));
        DefaultMultipartHttpServletRequest mrequest = (DefaultMultipartHttpServletRequest) actual;
        MultipartParameters params = mrequest.getMultipartParameters();
        FileItemMultipartFile actualFile = (FileItemMultipartFile) params.getFile("foo")[0];
        assertThat(actualFile.getFileItem(), is(fileItem));
        actualFile = (FileItemMultipartFile) params.getFile("baa")[0];
        assertThat(actualFile.getFileItem(), is(fileItem2));
        actualFile = (FileItemMultipartFile) params.getFile("baa")[1];
        assertThat(actualFile.getFileItem(), is(fileItem3));

        String actualParam = params.getParameter("baa")[0];
        String actualParam2 = params.getParameter("baz")[0];
        String actualParam3 = params.getParameter("baa")[1];

        assertThat(actualParam, is("ばー"));
        assertThat(actualParam2, is("ばず"));
        assertThat(actualParam3, is("ばぁあ"));
    }

    @Test
    public void testIsNotMultipartRequest() {

        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        contextFactory = new MultipartRequestContextFactory();
        HttpServletRequest actual = contextFactory.createRequestContext(servletContext, request,
                response).getRequest();

        assertThat(actual, is(request));
    }

    @Test
    public void testCreateRequestContextAndRaiseIOException() throws Exception {

        thrown.expect(FileUploadFailureException.class);

        // multi-part request.
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("multipart/form-data");
        when(request.getCharacterEncoding()).thenReturn("Unknown-Encoding");

        final FileItem param1 = mock(FileItem.class);
        when(param1.isFormField()).thenReturn(true);
        when(param1.getString("Unknown-Encoding")).thenThrow(new UnsupportedEncodingException());

        when(fileUploadFactory.createFileUpload(fileItemFactory)).thenReturn(new FileUpload() {
            @Override
            @SuppressWarnings("rawtypes")
            public List parseRequest(RequestContext context) {
                return Arrays.asList(param1);
            }
        });

        contextFactory = new MultipartRequestContextFactory();
        contextFactory.setFileItemFactory(fileItemFactory);
        contextFactory.setFileUploadFactory(fileUploadFactory);
        contextFactory.createRequestContext(servletContext, request, response);
    }

}
