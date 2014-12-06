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
import java.util.List;

import org.analogweb.Application;
import org.analogweb.ApplicationProperties;
import org.analogweb.InvocationMetadata;
import org.analogweb.Multipart;
import org.analogweb.Parameters;
import org.analogweb.RequestContext;
import org.analogweb.core.DefaultApplicationProperties;
import org.analogweb.core.MediaTypes;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class MultipartParameterResolverTest {

    private MultipartParameterResolver resolver;
    private RequestContext context;
    private InvocationMetadata metadata;
    private Parameters params;
    private Parameters empty;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ApplicationProperties config = DefaultApplicationProperties.defaultProperties();
        ApplicationPropertiesHolder.configure(mock(Application.class), config);
        resolver = new MultipartParameterResolver();
        context = mock(RequestContext.class);
        metadata = mock(InvocationMetadata.class);
        params = mock(Parameters.class);
        empty = mock(Parameters.class);
        when(context.getFormParameters()).thenReturn(params);
        when(context.getQueryParameters()).thenReturn(empty);
        when(context.getMatrixParameters()).thenReturn(empty);
        when(context.getRequestMethod()).thenReturn("POST");
    }

    @After
    public void tearDown() {
        ApplicationPropertiesHolder.dispose(mock(Application.class));
    }

    @Test
    public void testResolveAttributeParameterValue() {
        when(params.getValues("foo")).thenReturn(Arrays.asList("baa"));
        Object actual = resolver.resolveValue(context, metadata, "foo", String.class, null);
        assertThat((String) actual, is("baa"));
    }

    @Test
    public void testResolveAttributeParameterValues() {
        String[] expected = { "baa", "baz" };
        when(context.getRequestMethod()).thenReturn("GET");
        when(params.getValues("foo")).thenReturn(Arrays.asList(expected));
        Object actual = resolver.resolveValue(context, metadata, "foo", String[].class, null);
        assertThat((String[]) actual, is(expected));
    }

    @Test
    public void testResolveAttributeFileValueCreateFileUpload() {
        final FileItemFactory fileItemFactory = mock(FileItemFactory.class);
        resolver.setFileItemFactory(fileItemFactory);
        @SuppressWarnings("unchecked")
        FileUploadFactory<FileUpload> fileUploadFactory = mock(FileUploadFactory.class);
        FileItem item1 = mock(FileItem.class);
        when(item1.getFieldName()).thenReturn("baa");
        FileItem item2 = mock(FileItem.class);
        when(item2.getFieldName()).thenReturn("foo");
        final List<FileItem> items = Arrays.asList(item1, item2);
        FileUpload fileUpload = new FileUpload() {

            @Override
            public List<FileItem> parseRequest(org.apache.commons.fileupload.RequestContext ctx)
                    throws FileUploadException {
                return items;
            }

            @Override
            public FileItemIterator getItemIterator(org.apache.commons.fileupload.RequestContext ctx)
                    throws FileUploadException, IOException {
                return null;
            }
        };
        when(fileUploadFactory.createFileUpload(fileItemFactory)).thenReturn(fileUpload);
        resolver.setFileUploadFactory(fileUploadFactory);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(null);
        when(context.getContentType()).thenReturn(MediaTypes.valueOf("multipart/form-data"));
        when(context.getCharacterEncoding()).thenReturn("Shift-JIS");
        when(context.getRequestMethod()).thenReturn("POST");
        Object actual = resolver.resolveValue(context, metadata, "foo", Multipart.class, null);
        Multipart actualMultipart = (Multipart) actual;
        assertThat(actualMultipart.getName(), is("foo"));
    }

    @Test
    public void testResolveAttributeFileValue() {
        Multipart file = mock(Multipart.class);
        @SuppressWarnings("unchecked")
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file });
        Object actual = resolver.resolveValue(context, metadata, "foo", Multipart.class, null);
        assertThat((Multipart) actual, is(file));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithMultipartArray() {
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });
        Object actual = resolver.resolveValue(context, metadata, "foo", Multipart[].class, null);
        Multipart[] actualFiles = (Multipart[]) actual;
        assertThat(actualFiles[0], is(file));
        assertThat(actualFiles[1], is(file2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithMultipart() {
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });
        Object actual = resolver.resolveValue(context, metadata, "foo", Multipart.class, null);
        Multipart actualFiles = (Multipart) actual;
        assertThat(actualFiles, is(file));
    }

    @Test
    @SuppressWarnings("unchecked")
    @Ignore
    public void testResolveAttributeWithFileArray() throws IOException {
        File repository = folder.newFolder();
        DiskFileItem disk1 = new DiskFileItem("foo", "text/plain", true, null, 8, repository);
        DiskFileItem disk2 = new DiskFileItem("foo", "text/plain", true, null, 1024, repository);
        Multipart file = new FileItemMultipart(disk1);
        Multipart file2 = new FileItemMultipart(disk2);
        disk1.getOutputStream().write("Hello file!".getBytes());
        disk2.getOutputStream().write("Hello file2!".getBytes());
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });
        Object actual = resolver.resolveValue(context, metadata, "foo", File[].class, null);
        File[] actualFiles = (File[]) actual;
        assertThat(fileToString(actualFiles[0]), is("Hello file!"));
        assertThat(fileToString(actualFiles[1]), is("Hello file2!"));
        assertThat(actualFiles.length, is(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithFile() throws IOException {
        File repository = folder.newFolder();
        DiskFileItem disk1 = new DiskFileItem("foo", "text/plain", true, null, 8, repository);
        DiskFileItem disk2 = new DiskFileItem("foo", "text/plain", true, null, 1024, repository);
        Multipart file = new FileItemMultipart(disk1);
        Multipart file2 = new FileItemMultipart(disk2);
        disk1.getOutputStream().write("Hello file!".getBytes());
        disk2.getOutputStream().write("Hello file2!".getBytes());
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });
        Object actual = resolver.resolveValue(context, metadata, "foo", File.class, null);
        File actualFiles = (File) actual;
        assertThat(fileToString(actualFiles), is("Hello file!"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeAsStream() throws IOException {
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("Hello file!".getBytes()));
        when(file2.getInputStream())
                .thenReturn(new ByteArrayInputStream("Hello file2!".getBytes()));
        when(file.getName()).thenReturn("file");
        when(file2.getName()).thenReturn("file2");
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });
        Object actual = resolver.resolveValue(context, metadata, "foo", InputStream.class, null);
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
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        when(file.getBytes()).thenReturn("Hello file!".getBytes());
        when(file2.getBytes()).thenReturn("Hello file2!".getBytes());
        when(file.getName()).thenReturn("file");
        when(file2.getName()).thenReturn("file2");
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });
        Object actual = resolver.resolveValue(context, metadata, "foo", byte[].class, null);
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
        @SuppressWarnings("unchecked")
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(null);
        Object actual = resolver.resolveValue(context, metadata, "foo", String.class, null);
        assertNull(actual);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testResolveAttributeMultipartParameters() {
        MultipartParameters params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        Object actual = resolver.resolveValue(context, metadata, "", Iterable.class, null);
        assertThat((MultipartParameters) actual, is(params));
    }

    @Test
    public void testResolveAttributeRequiresMultipart() {
        @SuppressWarnings("rawtypes")
        MultipartParameters params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        Multipart multipart = mock(Multipart.class);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { multipart });
        Object actual = resolver.resolveValue(context, metadata, "foo", Multipart.class, null);
        assertThat((Multipart) actual, is(multipart));
    }

    @Test
    public void testResolveAttributeNotMultipartRequestWithoutParameter() {
        @SuppressWarnings("rawtypes")
        MultipartParameters params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(null);
        Object actual = resolver.resolveValue(context, metadata, "foo", Multipart.class, null);
        assertNull(actual);
    }

    @Test
    public void testResolveAttributeWithoutServletRequestContext() {
        RequestContext requestContext = mock(RequestContext.class);
        Parameters params = mock(Parameters.class);
        when(requestContext.getQueryParameters()).thenReturn(params);
        when(params.getValues("foo")).thenReturn(Arrays.asList("baa"));
        Object actual = resolver.resolveValue(requestContext, metadata, "foo", Multipart.class,
                null);
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
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        Multipart multipart = mock(Multipart.class);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { multipart });
        Object actual = resolver.resolveValue(context, metadata, "foo", InputStream[].class, null);
        assertThat((Multipart) actual, is(multipart));
    }
}
