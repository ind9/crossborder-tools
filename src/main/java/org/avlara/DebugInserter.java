package org.avlara;

public class DebugInserter implements SqlInserter {

    @Override
    public void insert(Model code) {

        if(code instanceof  Code) {
            System.out.println("Code : "+code.toString() );
        }else if(code instanceof Rate ) {
            System.out.println("Rate :"+code.toString());
        }
    }
}
