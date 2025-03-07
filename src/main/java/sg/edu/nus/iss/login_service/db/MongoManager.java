package sg.edu.nus.iss.login_service.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MongoManager {
    private static final Logger log = LoggerFactory.getLogger(MongoManager.class);

    public MongoClient getMongoClient(String dbName) {
        return MongoSingleton.getMongoClient(dbName);
    }

    public Document findDocument(Document query, String dbName, String collectionName) {
        try {
            MongoDatabase database = getMongoClient(dbName).getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(collectionName);
            return collection.find(query).first();
        } catch (Exception ex) {
            log.error("Error finding document in {}: {}", collectionName, ex.getMessage());
            return null;
        }
    }

    public Document findOneAndUpdate(Document query, Document update, String dbName, String collectionName,
                                     boolean upsert, boolean returnUpdatedDoc) {
        try {
            MongoDatabase database = getMongoClient(dbName).getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                    .returnDocument(returnUpdatedDoc ? ReturnDocument.AFTER : ReturnDocument.BEFORE)
                    .upsert(upsert);

            return collection.findOneAndUpdate(query, update, options);
        } catch (Exception ex) {
            log.error("Error updating document in {}: {}", collectionName, ex.getMessage());
            return null;
        }
    }
}
