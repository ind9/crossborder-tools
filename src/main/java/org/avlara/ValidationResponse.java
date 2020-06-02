package org.avlara;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationResponse {
    private List<Validation> errors  = new ArrayList<>();
    private List<Validation> summary = new ArrayList<>();
}
