package org.analogweb.acf;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.Application;
import org.analogweb.ApplicationProperties;
import org.analogweb.Multipart;
import org.analogweb.ServletRequestContext;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.analogweb.util.ApplicationPropertiesHolder.Creator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TemporaryUploadFolderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ServletRequestContext context;
    private Multipart multipartFile;

    private Application app;
    private ApplicationProperties props;
    private Creator creator;
    private HttpServletRequest request;

    @Before
    public void setUp() {
        context = mock(ServletRequestContext.class);
        multipartFile = mock(Multipart.class);
        creator = mock(Creator.class);
        app = mock(Application.class);
        props = mock(ApplicationProperties.class);
        when(creator.create()).thenReturn(props);
        ApplicationPropertiesHolder.configure(app, creator);
        request = mock(HttpServletRequest.class);
    }

    @After
    public void tearDown() {
        ApplicationPropertiesHolder.dispose(app);
    }

    @Test
    @SuppressWarnings("resource")
    public void testRequire() throws Exception {

        when(context.getServletRequest()).thenReturn(request);

        File testFolder = folder.newFolder("test1");
        when(props.getTempDir()).thenReturn(testFolder);
        when(request.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(null).thenReturn(
                new TemporaryUploadFolder(testFolder));

        doNothing().when(request).setAttribute(eq(TemporaryUploadFolder.TMP_DIR),
                isA(TemporaryUploadFolder.class));

        TemporaryUploadFolder file = TemporaryUploadFolder.current(context);

        when(multipartFile.getName()).thenReturn("some");
        when(multipartFile.getInputStream()).thenReturn(
                new ByteArrayInputStream("this is test!".getBytes()));

        File actual = file.require(multipartFile);
        assertThat(actual.exists(), is(true));
        assertThat(new BufferedReader(new FileReader(actual)).readLine(), is("this is test!"));

        file.dispose();
        assertThat(actual.exists(), is(false));
    }

    @Test
    @SuppressWarnings("resource")
    public void testRequireSecond() throws Exception {

        when(context.getServletRequest()).thenReturn(request);

        File testFolder = folder.newFolder("test1");
        when(props.getTempDir()).thenReturn(testFolder);
        when(request.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(
                new TemporaryUploadFolder(testFolder));

        doNothing().when(request).setAttribute(eq(TemporaryUploadFolder.TMP_DIR),
                isA(TemporaryUploadFolder.class));

        TemporaryUploadFolder file = TemporaryUploadFolder.current(context);

        when(multipartFile.getName()).thenReturn("some");
        when(multipartFile.getInputStream()).thenReturn(
                new ByteArrayInputStream("this is test!".getBytes()));

        File actual = file.require(multipartFile);
        assertThat(new BufferedReader(new FileReader(actual)).readLine(), is("this is test!"));
    }

    @Test
    @SuppressWarnings("resource")
    public void testSimplyRequire() throws Exception {

        when(context.getServletRequest()).thenReturn(request);

        File testFolder = folder.newFolder("test1");
        when(props.getTempDir()).thenReturn(testFolder);
        when(request.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(null).thenReturn(
                new TemporaryUploadFolder(testFolder));

        doNothing().when(request).setAttribute(eq(TemporaryUploadFolder.TMP_DIR),
                isA(TemporaryUploadFolder.class));

        when(multipartFile.getName()).thenReturn("some");
        File from = folder.newFile("some.txt");
        FileWriter fw = new FileWriter(from);
        fw.write("this is test!");
        fw.flush();
        fw.close();
        when(multipartFile.getInputStream()).thenReturn(new FileInputStream(from));

        File actual = TemporaryUploadFolder.require(context, multipartFile);
        assertThat(new BufferedReader(new FileReader(actual)).readLine(), is("this is test!"));
    }

    @Test
    public void testRequireWithIOException() throws Exception {

        when(context.getServletRequest()).thenReturn(request);

        File testFolder = folder.newFolder("test1");
        when(props.getTempDir()).thenReturn(testFolder);
        when(request.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(null).thenReturn(
                new TemporaryUploadFolder(testFolder));

        doNothing().when(request).setAttribute(eq(TemporaryUploadFolder.TMP_DIR),
                isA(TemporaryUploadFolder.class));

        TemporaryUploadFolder file = TemporaryUploadFolder.current(context);

        when(multipartFile.getName()).thenReturn("some");
        when(multipartFile.getInputStream()).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException();
            }
        });

        File actual = file.require(multipartFile);
        assertThat(actual, is(nullValue()));
    }

}
