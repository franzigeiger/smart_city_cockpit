package de.se.main;


import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main  {

    private static URI BASE_URI = URI.create("http://localhost:8080/rest/");

    public static void main(String[] args) throws IOException, InterruptedException {
        // Use this method for initalization, because it will also called by start with Tomcat
        AppInitialization.start();

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                BASE_URI, createApp(), false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdownNow()));
        server.start();

        System.out.println(String.format("\nGrizzly-HTTP-Server gestartet mit der URL: %s\n"
                        + "Stoppen des Grizzly-HTTP-Servers mit:      Strg+C\n",
                BASE_URI));

        Thread.currentThread().join();
    }

    private static ResourceConfig createApp() {
        // create a resource config that scans for JAX-RS resources and providers
        return new ResourceConfig()
                .packages("de/se/services");
    }





}

