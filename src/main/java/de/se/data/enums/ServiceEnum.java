package de.se.data.enums;

/**
 * This enum contains the type of possible service requests.
 */
public enum ServiceEnum {
    VehicleMaintenance,
    StopMaintenance,
    VehicleCleaning,
    StopCleaning;

    @Override
    public String toString() { //for nicer formatting
        switch(this) {
            case VehicleMaintenance:
                return "Vehicle Maintenance";
            case StopMaintenance:
                return "Stop Maintenance";
            case VehicleCleaning:
                return "Vehicle Cleaning";
            case StopCleaning:
                return "Stop Cleaning";
            default:
                return "UNKNOWN";
        }
    }
}
