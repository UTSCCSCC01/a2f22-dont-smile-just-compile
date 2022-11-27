package ca.utoronto.utm.mcs;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoDao {
	
	public MongoCollection<Document> collection;

	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.
		Dotenv dotenv = Dotenv.load();
		String uriDb = dotenv.get("MONGODB_ADDR");
		String dbName = "trip";
		String collectionName = "trips";
		String mongoDB = String.format("mongodb://%s:%s@%s:27017", "root", "123456", uriDb);
		MongoClient mongoClient = MongoClients.create(mongoDB);
		MongoDatabase database = mongoClient.getDatabase(dbName);
		this.collection = database.getCollection(collectionName);

	}

	// *** implement database operations here *** //
	public FindIterable<Document> getTrips() {
		try {
			return this.collection.find();
		} catch (Exception e) {
			System.out.println("Error occured");
		}
		return null;
	}

	public String postTrip(String driver, String passenger, String startTime){
		Document doc = new Document();
		doc.put("driver", driver);
		doc.put("passenger", passenger);
		doc.put("startTime", startTime);
		try {
			this.collection.insertOne(doc);
			return doc.get("_id").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Document getTripByFilter(String key, Object value){
		return this.collection.find(Filters.eq(key, value)).first();
	}

	public FindIterable<Document> getTripsByFilter(String key, Object value){
		return this.collection.find(Filters.eq(key, value));
	}

	public void patchTrip(String tripId, double distance, int endTime, String timeElapsed, double discount, double totalCost,
							 double driverPayout){
		Document update = new Document();
		update.put("distance", distance);
		update.put("endTime", endTime);
		update.put("timeElapsed", timeElapsed);
		update.put("discount", discount);
		update.put("totalCost", totalCost);
		update.put("driverPayout", driverPayout);

		this.collection.updateOne(Filters.eq("_id", new ObjectId(tripId)), new Document("$set", update));
	}

}
