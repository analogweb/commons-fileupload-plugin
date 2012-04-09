package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.analogweb.MultipartFile;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestAttributes;
import org.analogweb.RequestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author snowgoose
 */
public class MultipartParametersTypeMapperTest extends
		MultipartParametersTypeMapper {

	private MultipartParametersTypeMapper typeMapper;
	private RequestContext context;
	private RequestAttributes attributes;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		typeMapper = new MultipartParametersTypeMapper();
		context = mock(RequestContext.class);
		attributes = mock(RequestAttributes.class);
	}

	@Test
	public void testTypeWithStream() throws Exception {
		MultipartFile multipartFile = mock(MultipartFile.class);
		File file = folder.newFile("a.txt");
		InputStream in = new FileInputStream(file);
		when(multipartFile.getInputStream()).thenReturn(in);
		InputStream actual = (InputStream) typeMapper.mapToType(context,
				attributes, multipartFile, InputStream.class, null);
		assertThat(actual, is(in));
	}

	@Test
	public void testTypeWithStreamNotRequiredTypeEquals() throws Exception {
		String parameter = "parameter";
		Object actual = typeMapper.mapToType(context,
				attributes, parameter, InputStream.class, null);
		assertThat(actual, is(nullValue()));
	}

	@Test
	public void testTypeWithBytes() throws Exception {
		MultipartFile multipartFile = mock(MultipartFile.class);
		byte[] bytes = new byte[0];
		when(multipartFile.getBytes()).thenReturn(bytes);
		byte[] actual = (byte[]) typeMapper.mapToType(context,
				attributes, multipartFile, byte[].class, null);
		assertThat(actual, is(bytes));
	}

	@Test
	public void testTypeWithMultipartParameters() throws Exception {
		MultipartParameters parameters = mock(MultipartParameters.class);
		MultipartParameters actual = (MultipartParameters) typeMapper
				.mapToType(context, attributes, parameters,
						MultipartParameters.class, null);
		assertThat(actual, is(parameters));
	}

	@Test
	public void testTypeWithMultipartParametersNotRequiredTypeEquals() throws Exception {
		MultipartFile multipartFile = mock(MultipartFile.class);
		Object actual = typeMapper.mapToType(context, attributes, multipartFile,
						MultipartParameters.class, null);
		assertThat(actual, is(nullValue()));
	}

}
