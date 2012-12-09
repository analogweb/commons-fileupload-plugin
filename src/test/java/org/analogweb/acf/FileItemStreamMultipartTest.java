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
        FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        when(item.getFieldName()).thenReturn("foo");
        String actual = file.getName();
        assertThat(actual, is("foo"));
    }

    @Test
    public void testGetFileName() {
        FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        when(item.getName()).thenReturn("foo.xml");
        String actual = file.getResourceName();
        assertThat(actual, is("foo.xml"));
    }

    @Test
    public void testGetInputStream() throws Exception {
        FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        InputStream in = mock(InputStream.class);
        when(item.openStream()).thenReturn(in);
        InputStream actual = file.getInputStream();
        assertThat(actual, is(in));
    }

    @Test
    public void testGetInputStreamAndThrowIOException() throws Exception {
        thrown.expect(FileUploadFailureException.class);
        FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        when(item.openStream()).thenThrow(new IOException());
        InputStream actual = file.getInputStream();
        assertNull(actual);
    }

    @Test
    public void testGetBytes() throws Exception {
        FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        InputStream in = new ByteArrayInputStream(new byte[] { 0x00, 0x01 });
        when(item.openStream()).thenReturn(in);
        byte[] actual = file.getBytes();
        assertThat(actual[0], is((byte) 0x00));
        assertThat(actual[1], is((byte) 0x01));
    }

    @Test
    public void testGetContentType() {
        FileItemStream item = mock(FileItemStream.class);
        file = new FileItemStreamMultipart(item);
        when(item.getContentType()).thenReturn("application/xml");
        String actual = file.getContentType();
        assertThat(actual, is("application/xml"));
    }

}
