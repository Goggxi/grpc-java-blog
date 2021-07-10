package com.goggxi.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class BlogSever {

    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(50053)
                .addService(new BlogServiceImpl())
                .addService(ProtoReflectionService.newInstance())
                .build();

        server.start();

        System.out.println("Server is running is port " + server.getPort());

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println("Received shutdown request");
                    server.shutdown();
                    System.out.println("Successfully stopped the server");
                })
        );

        server.awaitTermination();

    }

}
