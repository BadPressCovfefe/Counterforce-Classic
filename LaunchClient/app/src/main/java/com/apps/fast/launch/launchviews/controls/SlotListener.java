package com.apps.fast.launch.launchviews.controls;

/**
 * Created by tobster on 23/01/17.
 */

public interface SlotListener
{
    void SlotClicked(int lSlotNumber);
    void SlotLongClicked(int lSlotNumber);
    boolean GetSlotOccupied(int lSlotNumber);
    String GetSlotContents(int lSlotNumber);
    boolean GetOnline();
    long GetSlotPrepTime(int lSlotNumber);
    SlotControl.ImageType GetImageType(int lSlotNumber);
}
