package org.analogweb.acf;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;

import org.analogweb.MultipartFile;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestContext;
import org.analogweb.TypeMapper;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

/**
 * {@link MultipartFile}を{@link File}、{@link InputStream}またはバイト配列
 * として変換する{@link TypeMapper}です。{@link MultipartParameters}の型が
 * 要求された場合は、変換元の値が{@link MultipartParameters}であったときだけ
 * インスタンスを返します。変換する対象のインスタンスでない場合は、nullを返します。
 * @author snowgoose
 */
public class MultipartParametersTypeMapper implements TypeMapper {

    private static final Log log = Logs.getLog(MultipartParametersTypeMapper.class);

    @Override
    public Object mapToType(RequestContext context, Object from, Class<?> requiredType,
            String[] formats) {
        log.log(CommonsFileUploadModulesConfig.PLUGIN_MESSAGE_RESOURCE, "TACF000006", from,
                requiredType);
        if (isEqualsType(InputStream.class, requiredType)) {
            return toStream(from);
        } else if (isEqualsType(MultipartParameters.class, requiredType)) {
            return toMultipartParameters(from);
        } else if (isEqualsType(byte[].class, requiredType)) {
            return toByteArray(from);
        } else if (isEqualsType(File.class, requiredType)) {
            return toFile(context, from);
        }
        return null;
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
