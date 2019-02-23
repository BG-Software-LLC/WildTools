package com.bgsoftware.wildtools.api.objects.tools;

@SuppressWarnings("unused")
public interface HarvesterTool extends Tool {

    int getRadius();

    int getFarmlandRadius();

    void setFarmlandRadius(int farmlandRadius);

    String getActivationAction();

    void setActivationAction(String activateAction);

}
