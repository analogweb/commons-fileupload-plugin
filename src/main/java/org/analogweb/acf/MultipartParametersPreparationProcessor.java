package org.analogweb.acf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.AttributesHandlers;
import org.analogweb.InvocationArguments;
import org.analogweb.InvocationMetadata;
import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestContext;
import org.analogweb.ServletRequestContext;
import org.analogweb.TypeMapperContext;
import org.analogweb.core.AbstractInvocationProcessor;

/**
 * {@link MultipartParameters}を現在のリクエストから解析し
 * コントローラの引数に設定する{@link AbstractInvocationProcessor}の実装です。<br/>
 * コントローラの引数は必ず{@link MultipartParameters}型で値を受け取る必要があります。
 * それ以外の型(サブタイプ等)が指定されていても値は適用されません。
 * @author snowgoose
 */
public class MultipartParametersPreparationProcessor extends AbstractInvocationProcessor {

    @Override
    public Object prepareInvoke(Method method, InvocationArguments args,
            InvocationMetadata metadata, RequestContext context, TypeMapperContext converters,
            AttributesHandlers handlers) {
        if (context instanceof ServletRequestContext) {
            HttpServletRequest request = ((ServletRequestContext) context).getServletRequest();
            if (request instanceof MultipartHttpServletRequest) {
                List<Integer> indexes = findIndexOfMultipartParameters(metadata.getArgumentTypes());
                if (indexes.isEmpty() == false) {
                    MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;
                    MultipartParameters params = multi.getMultipartParameters();
                    for (int index : indexes) {
                        args.putInvocationArgument(index, params);
                    }
                }
            }
        }
        return NO_INTERRUPTION;
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
