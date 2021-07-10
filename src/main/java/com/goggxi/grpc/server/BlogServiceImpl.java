package com.goggxi.grpc.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

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

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {

        System.out.println("Received read blog request");

        String blog_id = request.getBlogId();

        System.out.println("Searching for a blog ");
        Document result = null;

        try {
            result = collection.find(Filters.eq("_id", new ObjectId(blog_id))) .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null) {
            System.out.println("Blog not found !");
        } else {
            System.out.println("Blog found, sending response ");
            Blog blog = Blog.newBuilder()
                    .setAuthorId(result.getString("author_id"))
                    .setTitle(result.getString("title"))
                    .setContent(result.getString("content"))
                    .setId(blog_id)
                    .build();

            responseObserver.onNext(ReadBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build());

            responseObserver.onCompleted();
        }
    }
}
