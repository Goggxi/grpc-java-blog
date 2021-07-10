package com.goggxi.grpc.client;

import com.proto.blog.*;
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
                .setAuthorId("test read")
                .setTitle("learning gRPC")
                .setContent("insert data to mongodb")
                .build();

        CreateBlogResponse createBlogResponse =  blogClient.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(blog )
                .build());

        System.out.println("Received create blog response");
        System.out.println(createBlogResponse.toString() );

        String blogId = createBlogResponse .getBlog().getId();

        System.out.println("Reading blog ..");
        ReadBlogResponse readBlogResponse = blogClient.readBlog(ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println(readBlogResponse.toString());

//        trigger not found error
//        System.out.println("read blog with not existing id...");
//        ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(ReadBlogRequest.newBuilder()
//                .setBlogId("no-data")
//                .build());

//        System.out.println(readBlogResponseNotFound.toString() );

        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("test updated 2")
                .setTitle("learning gRPC (updated 2)")
                .setContent("insert data to mongodb (updated 2)")
                .build();

        System.out.println("Updating blog...");
        UpdateBlogResponse updateBlogResponse = blogClient.updateBlog(UpdateBlogRequest.newBuilder()
                .setBlog(newBlog)
                .build());

        System.out.println("Updated Blog");
        System.out.println(updateBlogResponse.toString());

    }

}
