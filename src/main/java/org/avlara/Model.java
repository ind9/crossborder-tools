package org.avlara;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class Model {
    private int row;
    private List<String> errorMessages  = new ArrayList<String>();
    private boolean hasError = false;
}
