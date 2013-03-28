package org.analogweb.acf;

import org.analogweb.ModulesBuilder;
import org.analogweb.PluginModulesConfig;
import org.analogweb.util.MessageResource;
import org.analogweb.util.PropertyResourceBundleMessageResource;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

/**
 * <a href="http://commons.apache.org/fileupload/">Apache commons fileUpload</a>
 * プラグインの既定の設定を行う{@link PluginModulesConfig}。<br/>
 * デフォルトでは、commons-fileuploadのStream APIと連動した
 * {@link MultipartParameterResolver}が指定されます。
 * @author snowgoose
 */
public class CommonsFileUploadModulesConfig implements PluginModulesConfig {

    public static final MessageResource PLUGIN_MESSAGE_RESOURCE = new PropertyResourceBundleMessageResource(
            "org.analogweb.acf.analog-messages");
    private static final Log log = Logs.getLog(CommonsFileUploadModulesConfig.class);

    @Override
    public ModulesBuilder prepare(ModulesBuilder builder) {
        log.log(PLUGIN_MESSAGE_RESOURCE, "IACF000001");
        builder.addInvocationProcessorClass(TemporaryUploadFolderDisposeProcessor.class);
        builder.addRequestValueResolverClass(MultipartParameterResolver.class);
        return builder;
    }
}
