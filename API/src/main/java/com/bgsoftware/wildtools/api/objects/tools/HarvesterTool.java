package com.bgsoftware.wildtools.api.objects.tools;

@SuppressWarnings("unused")
public interface HarvesterTool extends Tool {

    /**
     * Get the harvesting radius of the wand.
     */
    int getRadius();

    /**
     * Get the farmland harvesting radius of the wand.
     */
    int getFarmlandRadius();

    /**
     * Set the farmland harvesting radius of the wand.
     * @param farmlandRadius The new farmland harvesting radius.
     */
    void setFarmlandRadius(int farmlandRadius);

    /**
     * Get the activation action of the wand.
     * @return Can be RIGHT_CLICK or LEFT_CLICK.
     */
    String getActivationAction();

    /**
     * Set the activation action of the wand.
     * @param activateAction Can be RIGHT_CLICK or LEFT_CLICK.
     */
    void setActivationAction(String activateAction);

}
