package com.goggxi.grpc.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private final MongoDatabase mongoDatabase = mongoClient.getDatabase("mydb");
    private final MongoCollection<Document> collection = mongoDatabase.getCollection("blog");

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
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException());
        } else {
            System.out.println("Blog found, sending response ");
            Blog blog = documentToBlog(result);

            responseObserver.onNext(ReadBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build());

            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {

        System.out.println("Received Blog update");

        Blog blog = request.getBlog();

        String blogId = blog.getId();

        System.out.println("Searching for a blog so we can updated ");

        Document result = null;

        try {
            result = collection.find(Filters.eq("_id", new ObjectId(blogId))) .first();
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
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException());
        } else {
            Document replacement = new Document("author_id", blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent())
                    .append("_id", new ObjectId(blogId));

            System.out.println("Replacing data from database... ");
            collection.replaceOne(Filters.eq("_id", result.getObjectId("_id")), replacement);

            System.out.println("Replaced! Sending as a  response");
            responseObserver.onNext(
                    UpdateBlogResponse.newBuilder()
                            .setBlog(documentToBlog(replacement))
                            .build()
            );

            responseObserver.onCompleted();
        }

    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {

        System.out.println("Received delete blog...");
        String blogId = request.getBlogId();

        DeleteResult result = null;

        try {
            result = collection.deleteOne(Filters.eq("_id", new ObjectId(blogId)));
        } catch (Exception e) {
            System.out.println("blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result.getDeletedCount() == 0) {
            System.out.println("blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException());
        } else {
            System.out.println("blog was deleted");
            responseObserver.onNext(
                    DeleteBlogResponse.newBuilder()
                            .setBlogId(blogId)
                            .build()
            );

            responseObserver.onCompleted();
        }

    }

    private Blog documentToBlog(Document document) {
        return Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();
    }
}
