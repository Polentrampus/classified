package com.classified.exception.business;

import com.classified.exception.ErrorCode;

public class MapperException extends BusinessException{
    public MapperException(Class<?> entity, String requestName) {
        super(ErrorCode.FAILED_TO_MAP_ENTITY, String.format("The entity %s was not mapped, there was an error in the request %s",
                entity.getName(), requestName));
    }
}
