package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.InvocationMetadata;
import org.analogweb.Multipart;
import org.analogweb.Parameters;
import org.analogweb.RequestContext;
import org.analogweb.servlet.ServletRequestContext;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class MultipartParameterResolverTest {

    private MultipartParameterResolver resolver;
    private ServletRequestContext context;
    private HttpServletRequest multipartRequest;
    private InvocationMetadata metadata;
    private Parameters params;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        resolver = new MultipartParameterResolver();
        context = mock(ServletRequestContext.class);
        multipartRequest = mock(HttpServletRequest.class);
        metadata = mock(InvocationMetadata.class);
        params = mock(Parameters.class);
        when(context.getParameters()).thenReturn(params);
    }

    @Test
    public void testResolveAttributeParameterValue() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        when(multipartRequest.getAttribute(MultipartParameterResolver.KEY_IS_MULTIPART_CONTENT))
                .thenReturn(Boolean.FALSE);
        when(params.getValues("foo")).thenReturn(Arrays.asList("baa"));
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", String.class);

        assertThat((String) actual, is("baa"));
    }

    @Test
    public void testResolveAttributeParameterValues() {
        String[] expected = { "baa", "baz" };
        when(context.getServletRequest()).thenReturn(multipartRequest);
        when(multipartRequest.getAttribute(MultipartParameterResolver.KEY_IS_MULTIPART_CONTENT))
                .thenReturn(Boolean.FALSE);
        when(params.getValues("foo")).thenReturn(Arrays.asList(expected));
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", String[].class);

        assertThat((String[]) actual, is(expected));
    }

    @Test
    public void testResolveAttributeFileValueCreateFileUpload() {
        final FileItemFactory fileItemFactory = mock(FileItemFactory.class);
        resolver.setFileItemFactory(fileItemFactory);
        @SuppressWarnings("unchecked")
        FileUploadFactory<FileUpload> fileUploadFactory = mock(FileUploadFactory.class);
        FileItemStream item1 = mock(FileItemStream.class);
        when(item1.getFieldName()).thenReturn("baa");
        FileItemStream item2 = mock(FileItemStream.class);
        when(item2.getFieldName()).thenReturn("foo");
        final List<FileItemStream> items = Arrays.asList(item1, item2);
        FileUpload fileUpload = new FileUpload() {

            @Override
            public List<?> parseRequest(org.apache.commons.fileupload.RequestContext ctx)
                    throws FileUploadException {
                // TODO Auto-generated method stub
                return items;
            }

            @Override
            public FileItemIterator getItemIterator(org.apache.commons.fileupload.RequestContext ctx)
                    throws FileUploadException, IOException {
                return new FileItemIterator() {
                    Iterator<FileItemStream> i = items.iterator();

                    @Override
                    public FileItemStream next() throws FileUploadException, IOException {
                        return i.next();
                    }

                    @Override
                    public boolean hasNext() throws FileUploadException, IOException {
                        return i.hasNext();
                    }
                };
            }

        };
        when(fileUploadFactory.createFileUpload(fileItemFactory)).thenReturn(fileUpload);
        resolver.setFileUploadFactory(fileUploadFactory);

        when(context.getServletRequest()).thenReturn(multipartRequest);

        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                null);
        when(multipartRequest.getContentType()).thenReturn("multipart/form-data");
        when(multipartRequest.getCharacterEncoding()).thenReturn("Shift-JIS");
        when(multipartRequest.getMethod()).thenReturn("POST");

        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart.class);

        Multipart actualMultipart = (Multipart) actual;
        assertThat(actualMultipart.getName(), is("foo"));
    }

    @Test
    public void testResolveAttributeFileValue() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);

        @SuppressWarnings("unchecked")
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file });
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart.class);

        assertThat((Multipart) actual, is(file));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithMultipartArray() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });

        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart[].class);

        Multipart[] actualFiles = (Multipart[]) actual;
        assertThat(actualFiles[0], is(file));
        assertThat(actualFiles[1], is(file2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithMultipart() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });

        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart.class);

        Multipart actualFiles = (Multipart) actual;
        assertThat(actualFiles, is(file));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithFileArray() throws IOException {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("Hello file!".getBytes()));
        when(file2.getInputStream())
                .thenReturn(new ByteArrayInputStream("Hello file2!".getBytes()));
        when(file.getName()).thenReturn("file");
        when(file2.getName()).thenReturn("file2");
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });

        when(multipartRequest.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(
                new TemporaryUploadFolder(folder.newFolder()));

        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", File[].class);

        File[] actualFiles = (File[]) actual;
        assertThat(fileToString(actualFiles[0]), is("Hello file!"));
        assertThat(fileToString(actualFiles[1]), is("Hello file2!"));
        assertThat(actualFiles.length, is(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithFile() throws IOException {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("Hello file!".getBytes()));
        when(file2.getInputStream())
                .thenReturn(new ByteArrayInputStream("Hello file2!".getBytes()));
        when(file.getName()).thenReturn("file");
        when(file2.getName()).thenReturn("file2");
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });

        when(multipartRequest.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(
                new TemporaryUploadFolder(folder.newFolder()));

        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", File.class);

        File actualFiles = (File) actual;
        assertThat(fileToString(actualFiles), is("Hello file!"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeAsStream() throws IOException {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("Hello file!".getBytes()));
        when(file2.getInputStream())
                .thenReturn(new ByteArrayInputStream("Hello file2!".getBytes()));
        when(file.getName()).thenReturn("file");
        when(file2.getName()).thenReturn("file2");
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });

        when(multipartRequest.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(
                new TemporaryUploadFolder(folder.newFolder()));

        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", InputStream.class);

        InputStream actualFiles = (InputStream) actual;
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        while ((i = actualFiles.read()) > 0) {
            buffer.append((char) i);
        }
        actualFiles.close();
        assertThat(buffer.toString(), is("Hello file!"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeAsBytes() throws IOException {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        when(file.getBytes()).thenReturn("Hello file!".getBytes());
        when(file2.getBytes()).thenReturn("Hello file2!".getBytes());
        when(file.getName()).thenReturn("file");
        when(file2.getName()).thenReturn("file2");
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });

        when(multipartRequest.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(
                new TemporaryUploadFolder(folder.newFolder()));

        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", byte[].class);

        byte[] actualFiles = (byte[]) actual;
        assertThat(new String(actualFiles), is("Hello file!"));
    }

    private String fileToString(File file) throws IOException {
        FileReader reader = new FileReader(file);
        int i = 0;
        StringBuilder buffer = new StringBuilder();
        while ((i = reader.read()) > 0) {
            buffer.append((char) i);
        }
        reader.close();
        return buffer.toString();
    }

    @Test
    public void testResolveAttributeNotAvairableValue() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        @SuppressWarnings("unchecked")
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(null);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", String.class);

        assertNull(actual);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testResolveAttributeMultipartParameters() {
        MultipartParameters params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Object actual = resolver.resolveAttributeValue(context, metadata, "", Iterable.class);

        assertThat((MultipartParameters) actual, is(params));
    }

    @Test
    public void testResolveAttributeRequiresMultipart() {
        @SuppressWarnings("rawtypes")
        MultipartParameters params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart multipart = mock(Multipart.class);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { multipart });
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart.class);

        assertThat((Multipart) actual, is(multipart));
    }

    @Test
    public void testResolveAttributeNotMultipartRequestWithoutParameter() {
        @SuppressWarnings("rawtypes")
        MultipartParameters params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(context.getServletRequest()).thenReturn(multipartRequest);
        when(params.getMultiparts("foo")).thenReturn(null);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart.class);

        assertNull(actual);
    }

    @Test
    public void testResolveAttributeWithoutServletRequestContext() {
        RequestContext requestContext = mock(RequestContext.class);
        Parameters params = mock(Parameters.class);
        when(requestContext.getParameters()).thenReturn(params);
        when(params.getValues("foo")).thenReturn(Arrays.asList("baa"));
        Object actual = resolver.resolveAttributeValue(requestContext, metadata, "foo",
                Multipart.class);

        assertThat(actual.toString(), is("baa"));
    }

    @Test
    public void testResolveAttributeRequiresUnsupportedType() {
        thrown.expect(new BaseMatcher<UnsupportedParameterTypeException>() {
            @Override
            public boolean matches(Object obj) {
                if (obj instanceof UnsupportedParameterTypeException) {
                    UnsupportedParameterTypeException un = (UnsupportedParameterTypeException) obj;
                    assertThat(un.getSpecifiedType().getCanonicalName(),
                            is(InputStream[].class.getCanonicalName()));
                    assertThat(un.getMissedParameterName(), is("foo"));
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description desc) {
                // nop.
            }
        });
        @SuppressWarnings("rawtypes")
        MultipartParameters params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart multipart = mock(Multipart.class);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { multipart });
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo",
                InputStream[].class);

        assertThat((Multipart) actual, is(multipart));
    }

}
