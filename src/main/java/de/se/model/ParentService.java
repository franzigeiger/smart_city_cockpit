package de.se.model;

/**
 * This is the parent interface of all service interfaces. THis is necessary to treat them equal in serviceRegistry
 */
public interface ParentService {

    /*
    This method returns the name of the service.
     */
    String getName();

    /*
    This method triggers initialisation proces of the service, which means fetch persistent data and get a konsistent sate
     */
    void initialize();
}
