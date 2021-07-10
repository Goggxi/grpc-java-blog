package com.goggxi.grpc.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

    public static void main(String[] args) {
        System.out.println("Blog Client ...");
        BlogClient main = new BlogClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("ipang")
                .setTitle("learning gRPC")
                .setContent("insert data to mongodb")
                .build();

        CreateBlogResponse response =  blogClient.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(blog )
                .build());

        System.out.println("Received create blog response");
        System.out.println(response.toString() );

    }

}
