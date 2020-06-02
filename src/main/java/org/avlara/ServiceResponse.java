package org.avlara;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ServiceResponse<T> {
    private T data;
    private List<String> errors = new ArrayList<String>();
    public boolean isSuccess() { return errors.size() == 0; }
}
