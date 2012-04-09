
package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.InvocationMetadata;
import org.analogweb.MultipartFile;
import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestContext;
import org.analogweb.core.ParameterScopeRequestAttributesResolver;
import org.analogweb.util.StringUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

/**
 * リクエストが{@link MultipartHttpServletRequest}である場合に、マルチパートファイル等を
 * リクエストパラメータとして解決する{@link ParameterScopeRequestAttributesResolver}の拡張実装です。
 * 指定された名前に応じたリクエストパラメータ(パラメータ文字列または、マルチパートファイル)を返しますが、名前に
 * 空を指定した場合は、{@link MultipartParameters}のインスタンスを返します。
 * @author snowgoose
 */
public class MultipartRequestParameterResolver extends ParameterScopeRequestAttributesResolver {

    private static final Log log = Logs.getLog(MultipartRequestParameterResolver.class);

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveAttributeValue(RequestContext requestContext, InvocationMetadata metadata, String name) {
        HttpServletRequest request = requestContext.getRequest();
        if (request instanceof MultipartHttpServletRequest) {
            log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000001");
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            Object value = super.resolveAttributeValue(requestContext, metadata, name);
            if (value != null) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000004", new Object[] { name, value });
                return value;
            }
            if (StringUtils.isEmpty(name)) {
                return multipartRequest.getMultipartParameters();
            }
            value = multipartRequest.getFileParameterValues(name);
            if (value == null || ((List<MultipartFile>) value).isEmpty()) {
                value = multipartRequest.getFileParameter(name);
            }
            log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000004", new Object[] { name, value });
            return value;
        }
        log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000005");
        return super.resolveAttributeValue(requestContext, metadata, name);
    }

}
