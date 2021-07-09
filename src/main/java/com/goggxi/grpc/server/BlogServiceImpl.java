package com.goggxi.grpc.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase mongoDatabase = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = mongoDatabase.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

        System.out.println("Received created blog request");

        Blog blog =request.getBlog();

        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Inserting blog ...");
        // we insert (create) the document in mongodb
        collection.insertOne(doc);


        // we retrieve the mongodb generated id
        String id = doc.getObjectId("_id").toString();
        System.out.println("Inserted blog: " + id);

//        CreateBlogResponse response = CreateBlogResponse.newBuilder()
//                .setBlog(Blog.newBuilder()
//                        .setAuthorId(blog.getAuthorId())
//                        .setTitle(blog.getTitle())
//                        .setContent(blog.getContent()))
//                .build();

//        or

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id).build())
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }
}
