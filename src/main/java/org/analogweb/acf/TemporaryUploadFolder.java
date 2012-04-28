package org.analogweb.acf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;
import org.analogweb.ApplicationProperties;
import org.analogweb.MultipartFile;
import org.analogweb.RequestContext;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.analogweb.util.IOUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

public class TemporaryUploadFolder implements Serializable {

	private static final long serialVersionUID = 1950934123601946837L;

	private static final Log log = Logs.getLog(TemporaryUploadFolder.class);

	protected static final String TMP_DIR = TemporaryUploadFolder.class
			.getPackage().getName() + "_" + "TMP_DIR";

	private File baseDir;

	public static TemporaryUploadFolder current(RequestContext context) {
		Object tmpDir = context.getRequest().getAttribute(TMP_DIR);
		if (!(tmpDir instanceof TemporaryUploadFolder)) {
			ApplicationProperties props = ApplicationPropertiesHolder.current();
			TemporaryUploadFolder newFolder = new TemporaryUploadFolder(
					new File(createCurrentDirName(props, context)));
			context.getRequest().setAttribute(TMP_DIR, newFolder);
			tmpDir = context.getRequest().getAttribute(TMP_DIR);
			log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000005", newFolder
					.getBaseDir().toString());
		}
		return (TemporaryUploadFolder) tmpDir;
	}

	private static synchronized String createCurrentDirName(
			ApplicationProperties props, RequestContext context) {
		return props.getTempDir().getPath() + "/"
				+ TemporaryUploadFolder.class.getName() + "_"
				+ System.currentTimeMillis() + "_" + context.hashCode();
	}

	protected TemporaryUploadFolder(File baseDir) {
		this.baseDir = baseDir;
	}

	protected File getBaseDir() {
		return this.baseDir;
	}

	public File require(MultipartFile multipartFile) {
		File file = new File(getBaseDir().getPath() + "/"
				+ multipartFile.getParameterName());
		if (!file.exists()) {
			FileOutputStream out = null;
			InputStream in = multipartFile.getInputStream();
			try {
				out = new FileOutputStream(file);
				IOUtils.copy(in, out);
			} catch (IOException e) {
				log.log(PLUGIN_MESSAGE_RESOURCE, "WACF000002",
						multipartFile.getParameterName());
				log.log(PLUGIN_MESSAGE_RESOURCE, "WACF000003", e, getBaseDir());
				return null;
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}
		return file;
	}

	public static File require(RequestContext context,
			MultipartFile multipartFile) {
		return current(context).require(multipartFile);
	}

}
