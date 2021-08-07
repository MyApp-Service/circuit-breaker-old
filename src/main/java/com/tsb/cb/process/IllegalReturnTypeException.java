package com.tsb.cb.process;

public class IllegalReturnTypeException extends IllegalArgumentException {

    public IllegalReturnTypeException(Class<?> returnType, String methodName, String explanation) {
        super(String.join(" ", returnType.getName(), methodName,
            "has unsupported by @TimeLimiter return type.", explanation));
    }
}