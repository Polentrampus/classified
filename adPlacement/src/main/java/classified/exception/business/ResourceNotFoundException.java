package classified.exception.business;

import classified.exception.ErrorCode;

import java.util.Arrays;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, String param, Object ... identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s with '%s' '%s' not found",
                        resourceName,
                        param,
                        Arrays.toString(identifier)));
    }
}