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

import javax.servlet.http.HttpServletRequest;

import org.analogweb.InvocationMetadata;
import org.analogweb.Multipart;
import org.analogweb.Parameters;
import org.analogweb.RequestContext;
import org.analogweb.ServletRequestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MultipartParameterResolverTest {

    private MultipartParameterResolver resolver;
    private ServletRequestContext context;
    private HttpServletRequest multipartRequest;
    private InvocationMetadata metadata;
    private Parameters params;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

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
    public void testResolveAttributeFileValue() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);

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
        return buffer.toString();
    }

    @Test
    public void testResolveAttributeNotAvairableValue() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(null);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", String.class);

        assertNull(actual);
    }

    @Test
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

}
