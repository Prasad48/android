package com.useriq.sdk.walkthrough;

import java.util.ArrayList;

public class WTPlacement {

    @WTStepView.WTPlacementType
    int type = WTStepView.WTPlacementType.AUTO;
    boolean isInside;
    @WTStepView.WTLocation
    int location;
    ArrayList<Long> offset;

    public WTPlacement(@WTStepView.WTPlacementType int type, boolean isInside, @WTStepView.WTLocation int location, ArrayList<Long> offset) {
        this.type = type;
        this.isInside = isInside;
        this.location= location;
        this.offset = offset;
    }
}
