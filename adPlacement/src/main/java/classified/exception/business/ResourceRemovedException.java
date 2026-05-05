package classified.exception.business;

import classified.exception.ErrorCode;

import java.util.Arrays;

public class ResourceRemovedException extends BusinessException {
    public ResourceRemovedException(String resourceName, String param, Object ... identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s with '%s' '%s' removed",
                        resourceName,
                        param,
                        Arrays.toString(identifier)));
    }
}