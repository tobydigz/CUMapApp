package com.digz.cumapapp.navigation.directioning;

import com.mapzen.valhalla.Instruction;


public class DetermineDirection {
    private Instruction oldDirection, newDirection;

    public DetermineDirection(){

    }

    public void setOldDirection(Instruction oldDirection) {
        this.oldDirection = oldDirection;
    }

    public void setNewDirection(Instruction newDirection) {
        this.newDirection = newDirection;
    }

    public boolean isToGoLeft(){
        return determineDirectionIsLeft(oldDirection.getDirection(), newDirection.getDirection());
    }

    private boolean determineDirectionIsLeft(String old, String present) {
        if (old.equals("N")) {

            if (present.equals("N"))return resolveIsLeftWithBearing();
            return !(present.equals("NE") || present.equals("E") || present.equals("SE") || present.equals("S"));
        }

        if (old.equals("NE")) {
            if (present.equals("NE"))return resolveIsLeftWithBearing();
            return !(present.equals("E") || present.equals("SE") || present.equals("S") || present.equals("SW"));
        }

        if (old.equals("E")) {
            if (present.equals("E"))return resolveIsLeftWithBearing();
            return !(present.equals("SE") || present.equals("S") || present.equals("SW") || present.equals("W"));
        }

        if (old.equals("SE")) {
            if (present.equals("SE"))return resolveIsLeftWithBearing();
            return !(present.equals("S") || present.equals("SW") || present.equals("W") || present.equals("NW"));
        }

        if (old.equals("S")) {
            if (present.equals("S"))return resolveIsLeftWithBearing();
            return !(present.equals("SW") || present.equals("W") || present.equals("NW") || present.equals("N"));
        }

        if (old.equals("SW")) {
            if (present.equals("SW"))return resolveIsLeftWithBearing();
            return !(present.equals("W") || present.equals("NW") || present.equals("N") || present.equals("NE"));
        }

        if (old.equals("W")) {
            if (present.equals("W"))return resolveIsLeftWithBearing();
            return !(present.equals("NW") || present.equals("N") || present.equals("NE") || present.equals("N"));
        }

        if (old.equals("NW")) {
            if (present.equals("NW"))return resolveIsLeftWithBearing();
            return !(present.equals("N") || present.equals("NE") || present.equals("E") || present.equals("SE"));
        }
        return true;
    }

    private boolean resolveIsLeftWithBearing() {
        return oldDirection.getBearing() > newDirection.getBearing();
    }

}
