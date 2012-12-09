package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.ApplicationProperties;
import org.analogweb.Multipart;
import org.analogweb.RequestContext;
import org.analogweb.ServletRequestContext;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.analogweb.util.IOUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

public class TemporaryUploadFolder implements Serializable {

    private static final long serialVersionUID = 1950934123601946837L;

    private static final Log log = Logs.getLog(TemporaryUploadFolder.class);

    protected static final String TMP_DIR = TemporaryUploadFolder.class.getPackage().getName()
            + "_" + "TMP_DIR";

    private File baseDir;

    public static TemporaryUploadFolder current(RequestContext context) {
        HttpServletRequest req;
        if (context instanceof ServletRequestContext) {
            req = ((ServletRequestContext) context).getServletRequest();
        } else {
            return null;
        }
        Object tmpDir = req.getAttribute(TMP_DIR);
        if (!(tmpDir instanceof TemporaryUploadFolder)) {
            ApplicationProperties props = ApplicationPropertiesHolder.current();
            TemporaryUploadFolder newFolder = new TemporaryUploadFolder(new File(
                    createCurrentDirName(props, context)));
            req.setAttribute(TMP_DIR, newFolder);
            tmpDir = req.getAttribute(TMP_DIR);
        }
        return (TemporaryUploadFolder) tmpDir;
    }

    private static synchronized String createCurrentDirName(ApplicationProperties props,
            RequestContext context) {
        return props.getTempDir().getPath() + "/" + TemporaryUploadFolder.class.getName() + "_"
                + System.currentTimeMillis() + "_" + context.hashCode();
    }

    protected TemporaryUploadFolder(File baseDir) {
        this.baseDir = baseDir;
    }

    protected File getBaseDir() {
        return this.baseDir;
    }

    public File require(Multipart multipart) {
        File base = getBaseDir();
        if (!base.exists()) {
            base.mkdirs();
            log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000005", base.toString());
        }
        File file = new File(base.getPath() + "/" + multipart.getName());
        if (!file.exists()) {
            FileOutputStream out = null;
            InputStream in = multipart.getInputStream();
            try {
                out = new FileOutputStream(file);
                IOUtils.copy(in, out);
                log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000006", file.getPath());
            } catch (IOException e) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "WACF000002", multipart.getName());
                log.log(PLUGIN_MESSAGE_RESOURCE, "WACF000003", e, base);
                return null;
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        } else {
            log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000007", file.getPath());
        }
        return file;
    }

    public static File require(RequestContext context, Multipart multipart) {
        return current(context).require(multipart);
    }

    public void dispose() {
        File baseDir = getBaseDir();
        if (baseDir != null && baseDir.exists()) {
            deleteRecurciverely(baseDir);
            baseDir.delete();
            log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000008", baseDir.getPath());
        }
    }

    private void deleteRecurciverely(File dir) {
        File[] listFiles = dir.listFiles();
        if (listFiles != null) {
            for (File f : listFiles) {
                if (f.isDirectory()) {
                    deleteRecurciverely(f);
                } else {
                    f.delete();
                }
            }
        }
    }

}
