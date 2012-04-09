package org.analogweb.acf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.Invocation;
import org.analogweb.InvocationMetadata;
import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestAttributes;
import org.analogweb.RequestContext;
import org.analogweb.TypeMapperContext;
import org.analogweb.core.AbstractInvocationProcessor;

/**
 * {@link MultipartParameters}を現在のリクエストから解析し
 * コントローラの引数に設定する{@link AbstractInvocationProcessor}の実装です。<br/>
 * コントローラの引数は必ず{@link MultipartParameters}型で値を受け取る必要があります。
 * それ以外の型(サブタイプ等)が指定されていても値は適用されません。
 * @author snowgoose
 */
@Deprecated
public class MultipartParametersPreparationProcessor extends AbstractInvocationProcessor {

    @Override
    public Invocation prepareInvoke(Method method, Invocation invocation,
            InvocationMetadata metadata, RequestContext context, RequestAttributes attributes,
            TypeMapperContext converters) {
        HttpServletRequest request = context.getRequest();
        if (request instanceof MultipartHttpServletRequest) {
            List<Integer> indexes = findIndexOfMultipartParameters(metadata.getArgumentTypes());
            if (indexes.isEmpty() == false) {
                MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;
                MultipartParameters params = multi.getMultipartParameters();
                for (int index : indexes) {
                    invocation.putPreparedArg(index, params);
                }
            }
        }
        return invocation;
    }

    private List<Integer> findIndexOfMultipartParameters(Class<?>[] argTypes) {
        List<Integer> result = new ArrayList<Integer>();
        Integer index = 0;
        for (Class<?> argType : argTypes) {
            if (argType.getCanonicalName().equals(MultipartParameters.class.getCanonicalName())) {
                result.add(index);
            }
            index++;
        }
        return result;
    }

}
