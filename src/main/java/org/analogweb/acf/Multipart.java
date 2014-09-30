package org.analogweb.acf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.analogweb.annotation.Resolver;
import org.analogweb.annotation.Valiables;
import org.analogweb.util.StringUtils;

/**
 * @author snowgooseyk
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Resolver(MultipartParameterResolver.class)
@Valiables
public @interface Multipart {

	String value() default StringUtils.EMPTY;
}
