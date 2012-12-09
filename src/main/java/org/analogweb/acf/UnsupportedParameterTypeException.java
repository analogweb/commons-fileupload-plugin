package org.analogweb.acf;

import org.analogweb.exception.MissingRequiredParameterException;

public class UnsupportedParameterTypeException extends MissingRequiredParameterException {

    private static final long serialVersionUID = -1988444875031666811L;
    private final Class<?> specifiedType;

    public UnsupportedParameterTypeException(String parameterName, Class<?> specifiedType) {
        super(parameterName);
        this.specifiedType = specifiedType;
    }

    public Class<?> getSpecifiedType() {
        return this.specifiedType;
    }
}
