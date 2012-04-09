package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.analogweb.acf.FileItemMultipartFile;
import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;

public class FileItemMultipartFileTest {

    private FileItem fileItem;
    
    @Before
    public void setUp() throws Exception {
        fileItem = mock(FileItem.class);
    }

    @Test
    public void testGetFileItem() {
        FileItemMultipartFile file = new FileItemMultipartFile(fileItem);
        FileItem actual = file.getFileItem();
        assertThat(actual, is(fileItem));
    }

    @Test
    public void testGetBytes() {
        byte[] expected = { 0x00, 0x01, 0x02 };
        when(fileItem.get()).thenReturn(expected);
        FileItemMultipartFile file = new FileItemMultipartFile(fileItem);
        byte[] actual = file.getBytes();
        assertThat(actual, is(expected));
    }

    @Test
    public void testGetContentType() {
        when(fileItem.getContentType()).thenReturn("UTF-8");
        FileItemMultipartFile file = new FileItemMultipartFile(fileItem);
        String actual = file.getContentType();
        assertThat(actual, is("UTF-8"));
    }

    @Test
    public void testGetFileName() {
        when(fileItem.getName()).thenReturn("upload-file");
        FileItemMultipartFile file = new FileItemMultipartFile(fileItem);
        String actual = file.getFileName();
        assertThat(actual, is("upload-file"));
    }

    @Test
    public void testGetParameterName() {
        when(fileItem.getFieldName()).thenReturn("upload-file-parameter");
        FileItemMultipartFile file = new FileItemMultipartFile(fileItem);
        String actual = file.getParameterName();
        assertThat(actual, is("upload-file-parameter"));
    }

    @Test
    public void testGetInputStream() throws Exception {
        InputStream expected = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        when(fileItem.getInputStream()).thenReturn(expected);
        FileItemMultipartFile file = new FileItemMultipartFile(fileItem);
        InputStream actual = file.getInputStream();
        assertThat(actual, is(expected));
    }

    @Test
    public void testGetInputStreamThrowsException() throws Exception {
        when(fileItem.getInputStream()).thenThrow(new IOException());
        FileItemMultipartFile file = new FileItemMultipartFile(fileItem);
        InputStream actual = file.getInputStream();
        assertNull(actual);
    }

}
