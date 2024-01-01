/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.halab4dev;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author halab
 */
public class Application {

    private static final String COLLECTION_NAME = "example";

    public static void main(String[] args) {
        System.out.println("Connecting to database ...");
        String mongodbUri = System.getenv("MONGO_URI");
        String dbName = System.getenv("MONGO_DATABASE");
        MongoClient client = MongoClients.create(mongodbUri);
        MongoCollection<Document> collection = client.getDatabase(dbName).getCollection(COLLECTION_NAME);
        System.out.println("Inserting data ...");
        String id = insertData(collection);
        System.out.println("Querying data ...");
        getData(collection, id);
    }
    
    public static String insertData(MongoCollection<Document> collection) {
        collection.drop();
        Document doc = new Document("name", "Docker example")
                .append("description", "Docker compose example, include mongodb and a java application")
                .append("created_date", LocalDateTime.of(2024, Month.JANUARY, 1, 23, 5, 0))
                .append("last_run_time", LocalDateTime.now());
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
