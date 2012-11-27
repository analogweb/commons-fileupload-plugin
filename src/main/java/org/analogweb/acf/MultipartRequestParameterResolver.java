package org.analogweb.acf;

import static org.analogweb.acf.CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.InvocationMetadata;
import org.analogweb.MultipartFile;
import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestContext;
import org.analogweb.ServletRequestContext;
import org.analogweb.core.ParameterScopeRequestAttributesResolver;
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
    public Object resolveAttributeValue(RequestContext requestContext, InvocationMetadata metadata,
            String name, Class<?> requiredType) {
        if (requestContext instanceof ServletRequestContext) {
            HttpServletRequest request = ((ServletRequestContext) requestContext)
                    .getServletRequest();
            Object value = super
                    .resolveAttributeValue(requestContext, metadata, name, requiredType);
            if (value != null) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000004", new Object[] { name, value });
                return value;
            }
            if (request instanceof MultipartHttpServletRequest) {
                log.log(PLUGIN_MESSAGE_RESOURCE, "DACF000001");
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                if (isEqualsType(MultipartParameters.class, requiredType)) {
                    return multipartRequest.getMultipartParameters();
                }
                value = multipartRequest.getFileParameterValues(name);
                if (value == null || ((List<MultipartFile>) value).isEmpty()) {
                    value = multipartRequest.getFileParameter(name);
                }
                log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000004", new Object[] { name, value });
                log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "TACF000006",
                        value, requiredType);
                if (isEqualsType(InputStream.class, requiredType)) {
                    return toStream(value);
                } else if (isEqualsType(byte[].class, requiredType)) {
                    return toByteArray(value);
                } else if (isEqualsType(File.class, requiredType)) {
                    return toFile(requestContext, value);
                }
                return value;
            }
            log.log(PLUGIN_MESSAGE_RESOURCE, "TACF000005");
        }
        return super.resolveAttributeValue(requestContext, metadata, name, requiredType);
    }

    protected File toFile(RequestContext context, Object from) {
        MultipartFile mf = getSingleInstance(MultipartFile.class, from);
        if (mf != null) {
            return TemporaryUploadFolder.require(context, mf);
        }
        return null;
    }

    protected byte[] toByteArray(Object from) {
        if (MultipartFile.class.isInstance(from)) {
            return ((MultipartFile) from).getBytes();
        }
        return null;
    }

    private boolean isEqualsType(Class<?> clazz, Class<?> clazz2) {
        return clazz.getCanonicalName().equals(clazz2.getCanonicalName());
    }

    protected InputStream toStream(Object from) {
        if (MultipartFile.class.isInstance(from)) {
            return ((MultipartFile) from).getInputStream();
        }
        return null;
    }

    protected MultipartParameters toMultipartParameters(Object from) {
        if (MultipartParameters.class.isInstance(from)) {
            return (MultipartParameters) from;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSingleInstance(Class<T> type, Object obj) {
        if (obj == null) {
            return null;
        }
        if (type.isInstance(obj)) {
            log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "TACF000007", type);
            return (T) obj;
        }
        if (obj.getClass().isArray()) {
            obj = Array.get(obj, 0);
            if (type.isInstance(obj)) {
                log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "TACF000007", type);
                return (T) obj;
            }
        }
        if (List.class.isInstance(obj)) {
            List<?> list = (List<?>) obj;
            if (!list.isEmpty() && (type.isInstance(obj = list.get(0)))) {
                log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "TACF000007", type);
                return (T) obj;
            }
        }
        log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "TACF000008",
                File.class.getCanonicalName());
        return null;
    }
}
