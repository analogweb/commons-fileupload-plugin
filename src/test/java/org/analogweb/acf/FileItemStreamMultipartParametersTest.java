package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.analogweb.Multipart;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FileItemStreamMultipartParametersTest {

    private FileItemStreamMultipartParameters parameters;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testIterator() throws Exception {

        final FileItemIterator iterator = mock(FileItemIterator.class);
        FileItemStream file1 = mock(FileItemStream.class);
        FileItemStream file2 = mock(FileItemStream.class);
        FileItemStream file3 = mock(FileItemStream.class);
        FileItemStream file4 = mock(FileItemStream.class);
        FileItemStream param1 = mock(FileItemStream.class);
        FileItemStream param2 = mock(FileItemStream.class);
        FileItemStream param3 = mock(FileItemStream.class);
        FileItemStream param4 = mock(FileItemStream.class);

        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true)
                .thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true)
                .thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(file1).thenReturn(param1).thenReturn(param2)
                .thenReturn(file2).thenReturn(param3).thenReturn(file3).thenReturn(param4)
                .thenReturn(file4);

        // iteration#1
        when(file1.isFormField()).thenReturn(false);
        when(file1.getFieldName()).thenReturn("fooFile");
        when(file1.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x00, 0x01, 0x02 }));

        // iteration#2
        when(param1.isFormField()).thenReturn(true);
        when(param1.getFieldName()).thenReturn("fooParam");
        when(param1.openStream()).thenReturn(new ByteArrayInputStream("foo".getBytes()));

        // iteration#3
        when(param2.isFormField()).thenReturn(true);
        when(param2.getFieldName()).thenReturn("baaParam");
        when(param2.openStream()).thenReturn(new ByteArrayInputStream("baa".getBytes()));

        // iteration#4
        when(file2.isFormField()).thenReturn(false);
        when(file2.getFieldName()).thenReturn("fooFile");
        when(file2.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x10, 0x11, 0x12 }));

        // iteration#5
        when(param3.isFormField()).thenReturn(true);
        when(param3.getFieldName()).thenReturn("fooParam");
        when(param3.openStream()).thenReturn(new ByteArrayInputStream("foofoo".getBytes()));

        // iteration#6
        when(file3.isFormField()).thenReturn(false);
        when(file3.getFieldName()).thenReturn("baaFile");
        when(file3.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x20, 0x21, 0x22 }));

        // iteration#7
        when(param4.isFormField()).thenReturn(true);
        when(param4.getFieldName()).thenReturn("fooParam");
        when(param4.openStream()).thenReturn(new ByteArrayInputStream("foo".getBytes()));

        // iteration#8
        when(file4.isFormField()).thenReturn(false);
        when(file4.getFieldName()).thenReturn("fooFile");
        when(file4.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x30, 0x31, 0x32 }));

        parameters = new FileItemStreamMultipartParameters(iterator, "UTF-8");
        Iterator<FileItemStreamMultipart> actual = parameters.iterator();

        assertTrue(actual.hasNext());

        FileItemStreamMultipart param = actual.next();
        assertTrue(param.isMultipartFile());
        assertArrayEquals(param.getBytes(), new byte[] { 0x00, 0x01, 0x02 });
        assertTrue(actual.hasNext());

        param = actual.next();
        assertFalse(param.isMultipartFile());
        assertThat(new String(param.getBytes()), is("foo"));
        assertTrue(actual.hasNext());

        param = actual.next();
        assertFalse(param.isMultipartFile());
        assertThat(new String(param.getBytes()), is("baa"));
        assertTrue(actual.hasNext());

        param = actual.next();
        assertTrue(param.isMultipartFile());
        assertArrayEquals(param.getBytes(), new byte[] { 0x10, 0x11, 0x12 });
        assertTrue(actual.hasNext());

        param = actual.next();
        assertFalse(param.isMultipartFile());
        assertThat(new String(param.getBytes()), is("foofoo"));
        assertTrue(actual.hasNext());

        param = actual.next();
        assertTrue(param.isMultipartFile());
        assertArrayEquals(param.getBytes(), new byte[] { 0x20, 0x21, 0x22 });
        assertTrue(actual.hasNext());

        param = actual.next();
        assertFalse(param.isMultipartFile());
        assertThat(new String(param.getBytes()), is("foo"));
        assertTrue(actual.hasNext());

        param = actual.next();
        assertTrue(param.isMultipartFile());
        assertArrayEquals(param.getBytes(), new byte[] { 0x30, 0x31, 0x32 });

        // end of iterator.
        assertFalse(actual.hasNext());
    }

    @Test
    public void testIteratorHasNextFailedOnIOException() throws Exception {
        thrown.expect(FileUploadFailureException.class);
        FileItemIterator iterator = mock(FileItemIterator.class);
        when(iterator.hasNext()).thenThrow(new IOException());
        parameters = new FileItemStreamMultipartParameters(iterator, "UTF-8");
        parameters.iterator().hasNext();
    }

    @Test
    public void testIteratorsRemoveUnsupported() throws Exception {
        thrown.expect(UnsupportedOperationException.class);
        FileItemIterator iterator = mock(FileItemIterator.class);
        when(iterator.hasNext()).thenThrow(new IOException());
        parameters = new FileItemStreamMultipartParameters(iterator, "UTF-8");
        parameters.iterator().remove();
    }

    @Test
    public void testIteratorNextFailedOnIOException() throws Exception {
        thrown.expect(FileUploadFailureException.class);
        FileItemIterator iterator = mock(FileItemIterator.class);
        when(iterator.next()).thenThrow(new IOException());
        parameters = new FileItemStreamMultipartParameters(iterator, "UTF-8");
        parameters.iterator().next();
    }

    @Test
    public void testIteratorHasNextFailedOnFileUploadException() throws Exception {
        thrown.expect(FileUploadFailureException.class);
        FileItemIterator iterator = mock(FileItemIterator.class);
        when(iterator.hasNext()).thenThrow(new FileUploadException());
        parameters = new FileItemStreamMultipartParameters(iterator, "UTF-8");
        parameters.iterator().hasNext();
    }

    @Test
    public void testIteratorNextFailedOnFileUploadException() throws Exception {
        thrown.expect(FileUploadFailureException.class);
        FileItemIterator iterator = mock(FileItemIterator.class);
        when(iterator.next()).thenThrow(new FileUploadException());
        parameters = new FileItemStreamMultipartParameters(iterator, "UTF-8");
        parameters.iterator().next();
    }

    @Test
    public void testGetParameter() throws Exception {

        final FileItemIterator iterator = mock(FileItemIterator.class);
        FileItemStream file1 = mock(FileItemStream.class);
        FileItemStream param1 = mock(FileItemStream.class);
        FileItemStream param2 = mock(FileItemStream.class);
        FileItemStream param3 = mock(FileItemStream.class);

        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true)
                .thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(file1).thenReturn(param1).thenReturn(param2)
                .thenReturn(param3);

        // iteration#1
        when(file1.isFormField()).thenReturn(false);
        when(file1.getFieldName()).thenReturn("foo");
        when(file1.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x00, 0x01, 0x02 }));

        // iteration#2
        when(param1.isFormField()).thenReturn(true);
        when(param1.getFieldName()).thenReturn("baa");
        when(param1.openStream()).thenReturn(new ByteArrayInputStream("hoge".getBytes()));

        // iteration#3
        when(param2.isFormField()).thenReturn(true);
        when(param2.getFieldName()).thenReturn("baz");
        when(param2.openStream()).thenReturn(new ByteArrayInputStream("fuga".getBytes()));

        // iteration#4
        when(param3.isFormField()).thenReturn(true);
        when(param3.getFieldName()).thenReturn("baa");
        when(param3.openStream()).thenReturn(new ByteArrayInputStream("boo".getBytes()));

        parameters = new FileItemStreamMultipartParameters(iterator, "UTF-8");
        String[] actual = parameters.getParameter("baa");

        assertThat(actual.length, is(2));
        assertThat(actual[0], is("hoge"));
        assertThat(actual[1], is("boo"));

        actual = parameters.getParameter("baz");

        assertThat(actual.length, is(1));
        assertThat(actual[0], is("fuga"));

        actual = parameters.getParameter("foo");
        assertNull(actual);

        Collection<String> names = parameters.getParameterNames();
        assertThat(names.size(), is(2));
        assertTrue(names.contains("baa"));
        assertTrue(names.contains("baz"));
    }

    @Test
    public void testGetParameterWithInvalidEncoding() throws Exception {
        
        thrown.expect(FileUploadFailureException.class);

        final FileItemIterator iterator = mock(FileItemIterator.class);
        FileItemStream file1 = mock(FileItemStream.class);
        FileItemStream param1 = mock(FileItemStream.class);

        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(file1).thenReturn(param1);

        // iteration#1
        when(file1.isFormField()).thenReturn(false);
        when(file1.getFieldName()).thenReturn("foo");
        when(file1.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x00, 0x01, 0x02 }));

        // iteration#2
        when(param1.isFormField()).thenReturn(true);
        when(param1.getFieldName()).thenReturn("baa");
        when(param1.openStream()).thenReturn(new ByteArrayInputStream("hoge".getBytes()));

        parameters = new FileItemStreamMultipartParameters(iterator, "UnknownEncoding");
        parameters.getParameter("baa");
    }

    @Test
    public void testGetFiles() throws Exception {

        final FileItemIterator iterator = mock(FileItemIterator.class);
        FileItemStream file1 = mock(FileItemStream.class);
        FileItemStream param1 = mock(FileItemStream.class);
        FileItemStream file2 = mock(FileItemStream.class);
        FileItemStream file3 = mock(FileItemStream.class);

        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true)
                .thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(file1).thenReturn(param1).thenReturn(file2)
                .thenReturn(file3);

        // iteration#1
        when(file1.isFormField()).thenReturn(false);
        when(file1.getFieldName()).thenReturn("foo");
        when(file1.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x00, 0x01, 0x02 }));

        // iteration#2
        when(param1.isFormField()).thenReturn(true);
        when(param1.getFieldName()).thenReturn("baa");
        when(param1.openStream()).thenReturn(new ByteArrayInputStream("hoge".getBytes()));

        // iteration#3
        when(file2.isFormField()).thenReturn(false);
        when(file2.getFieldName()).thenReturn("baz");
        when(file2.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x10, 0x11, 0x12 }));

        // iteration#4
        when(file3.isFormField()).thenReturn(false);
        when(file3.getFieldName()).thenReturn("foo");
        when(file3.openStream()).thenReturn(
                new ByteArrayInputStream(new byte[] { 0x30, 0x31, 0x32 }));

        parameters = new FileItemStreamMultipartParameters(iterator, "UTF-8");
        Multipart[] actual = parameters.getMultiparts("foo");

        assertThat(actual.length, is(2));
        assertArrayEquals(new byte[] { 0x00, 0x01, 0x02 }, actual[0].getBytes());
        assertArrayEquals(new byte[] { 0x30, 0x31, 0x32 }, actual[1].getBytes());

        actual = parameters.getMultiparts("baz");

        assertThat(actual.length, is(1));
        assertArrayEquals(new byte[] { 0x10, 0x11, 0x12 }, actual[0].getBytes());

        actual = parameters.getMultiparts("baa");
        assertNull(actual);

        Collection<String> names = parameters.getMultipartParameterNames();
        assertThat(names.size(), is(2));
        assertTrue(names.contains("foo"));
        assertTrue(names.contains("baz"));
    }

    
}
