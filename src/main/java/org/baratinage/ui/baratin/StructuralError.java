package org.baratinage.ui.baratin;

import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IStructuralError;

public class StructuralError extends BamItem implements IStructuralError {

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    public StructuralError() {
        super(TYPE);
    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStructuralErrorModel'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public void parentHasChanged(BamItem parent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parentHasChanged'");
    }

    @Override
    public String toJsonString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJsonString'");
    }

    @Override
    public void fromJsonString(String jsonString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJsonString'");
    }

}
