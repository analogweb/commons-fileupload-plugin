package org.analogweb.acf;

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
import java.io.FileReader;

import org.analogweb.Application;
import org.analogweb.ApplicationProperties;
import org.analogweb.Multipart;
import org.analogweb.RequestContext;
import org.analogweb.ResponseContext;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TemporaryUploadFolderDisposeProcessorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private final TemporaryUploadFolderDisposeProcessor processor = new TemporaryUploadFolderDisposeProcessor();
    private RequestContext context;
    private ResponseContext response;
    private Multipart multipartFile;
    private Application app;
    private ApplicationProperties props;

    @Before
    public void setUp() {
        context = mock(RequestContext.class);
        response = mock(ResponseContext.class);
        multipartFile = mock(Multipart.class);
        app = mock(Application.class);
        props = mock(ApplicationProperties.class);
        ApplicationPropertiesHolder.configure(app, props);
    }

    @After
    public void tearDown() {
        ApplicationPropertiesHolder.dispose(app);
    }

    @Test
    @SuppressWarnings("resource")
    public void testRequire() throws Exception {
        File testFolder = folder.newFolder("test1");
        when(props.getTempDir()).thenReturn(testFolder);
        when(context.getAttribute(TemporaryUploadFolder.TMP_DIR)).thenReturn(null).thenReturn(
                new TemporaryUploadFolder(testFolder));
        doNothing().when(context).setAttribute(eq(TemporaryUploadFolder.TMP_DIR),
                isA(TemporaryUploadFolder.class));
        TemporaryUploadFolder file = TemporaryUploadFolder.current(context);
        when(multipartFile.getName()).thenReturn("some");
        when(multipartFile.getInputStream()).thenReturn(
                new ByteArrayInputStream("this is test!".getBytes()));
        File actual = file.require(multipartFile);
        assertThat(actual.exists(), is(true));
        assertThat(new BufferedReader(new FileReader(actual)).readLine(), is("this is test!"));
        processor.afterCompletion(context, response, null);
        assertThat(actual.exists(), is(false));
    }
}
