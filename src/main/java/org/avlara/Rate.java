package org.avlara;

import lombok.Data;

@Data
public class Rate extends Model {
    private String citationTexts,
                   manufactureSourceType,
                   shippingDestinationType,
                   fKCodeId,
                   shippingSourceType,
                   formula,
                   formulaType,
                   taxSection,
                   fKShippingSourceGroupId,
                   fKShippingSourceCountryId;


    public String ts() {
        return super.toString() + this.toString();
    }

}
