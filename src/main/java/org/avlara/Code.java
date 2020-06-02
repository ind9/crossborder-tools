package org.avlara;

import lombok.Data;

@Data
public class Code extends Model {
    private String  description,
                    fkCodeId,
                    sequenceNumber,
                    parsedCode;

    private boolean isSystemDefined,
                    isTaxable,
                    hasChildren,
                    isZeroPadded,
                    isDecision;

    private int     zeroPaddedCount;


    public String ts() {
        return super.toString() + this.toString();
    }
}
