package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItemStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FileItemStreamMultipartTest {

    private FileItemStreamMultipart file;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetParameterName() {
        final FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        when(item.getFieldName()).thenReturn("foo");
        final String actual = file.getName();
        assertThat(actual, is("foo"));
    }

    @Test
    public void testGetFileName() {
        final FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        when(item.getName()).thenReturn("foo.xml");
        final String actual = file.getResourceName();
        assertThat(actual, is("foo.xml"));
    }

    @Test
    public void testGetInputStream() throws Exception {
        final FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        final InputStream in = mock(InputStream.class);
        when(item.openStream()).thenReturn(in);
        final InputStream actual = file.getInputStream();
        assertThat(actual, is(in));
    }

    @Test
    public void testGetInputStreamAndThrowIOException() throws Exception {
        thrown.expect(FileUploadFailureException.class);
        final FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        when(item.openStream()).thenThrow(new IOException());
        final InputStream actual = file.getInputStream();
        assertNull(actual);
    }

    @Test
    public void testGetBytes() throws Exception {
        final FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        final InputStream in = new ByteArrayInputStream(new byte[] { 0x00, 0x01 });
        when(item.openStream()).thenReturn(in);
        final byte[] actual = file.getBytes();
        assertThat(actual[0], is((byte) 0x00));
        assertThat(actual[1], is((byte) 0x01));
    }

    @Test
    public void testGetContentType() {
        final FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        when(item.getContentType()).thenReturn("application/xml");
        final String actual = file.getContentType();
        assertThat(actual, is("application/xml"));
    }
}
