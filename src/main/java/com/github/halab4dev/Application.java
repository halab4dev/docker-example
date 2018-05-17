/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.halab4dev;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import java.util.Date;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author halab
 */
public class Application {

    private static final String DB_HOST = "mongo-docker";
    private static final int DB_PORT = 27017;
    private static final String DB_USER = "halab";
    private static final String DB_PASSWORD = "superman";
    private static final String DB_AUTHENTICATION_DB = "admin";

    private static final String DB_NAME = "docker_example";
    private static final String COLLECTION_NAME = "example";

    public static void main(String[] args) {
        System.out.println("Connecting to database ...");
        MongoClient client = connectDatabase();
        MongoCollection<Document> collection = client.getDatabase(DB_NAME).getCollection(COLLECTION_NAME);
        System.out.println("Inserting data ...");
        String id = insertData(collection);
        System.out.println("Querying data ...");
        getData(collection, id);
    }

    public static MongoClient connectDatabase() {
        MongoCredential credential = MongoCredential.createCredential(DB_USER, DB_AUTHENTICATION_DB, DB_PASSWORD.toCharArray());
        MongoClientOptions option = new MongoClientOptions.Builder().build();
        return new MongoClient(new ServerAddress(DB_HOST, DB_PORT), credential, option);
    }
    
    public static String insertData(MongoCollection<Document> collection) {
        collection.drop();
        Document doc = new Document("name", "Docker example")
                .append("description", "Docker compose example, include mongodb and a java application")
                .append("created_date", new Date(118, 4, 17))
                .append("last_run_time", new Date());
        collection.insertOne(doc);
        return doc.getObjectId("_id").toString();
    }

    public static void getData(MongoCollection<Document> collection, String id) {
        Document query = new Document("_id", new ObjectId(id));
        Document data = collection.find(query).first();
        System.out.println("===== Application information =====");
        System.out.println("Name: " + data.getString("name"));
        System.out.println("Description: " + data.getString("description"));
        System.out.println("Created date: " + data.getDate("created_date"));
        System.out.println("Last run time: " + data.getDate("last_run_time"));
    }

}
