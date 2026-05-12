package com.classified.exception.business;

import com.classified.exception.ErrorCode;

public class FieldSortingException extends BusinessException{
    public FieldSortingException() {
        super(ErrorCode.INCORRECT_SETTINGS_SORT, "The field is not allowed for sorting.");
    }
}
