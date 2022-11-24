package ca.utoronto.utm.mcs;

import org.neo4j.driver.*;
import io.github.cdimascio.dotenv.Dotenv;

public class Neo4jDAO {

    private final Session session;
    private final Driver driver;
    private final String username = "neo4j";
    private final String password = "123456";

    public Neo4jDAO() {
        Dotenv dotenv = Dotenv.load();
        String addr = dotenv.get("NEO4J_ADDR");
        String uriDb = "bolt://" + addr + ":7687";

        this.driver = GraphDatabase.driver(uriDb, AuthTokens.basic(this.username, this.password));
        this.session = this.driver.session();
    }

    // *** implement database operations here *** //

    public Result getNearbyDrivers(String uid, int radius) {
        String query = "MATCH (n: user {uid: '%s'}), (other: user {is_driver: true}) " +
                "WITH point({x: n.longitude, y: n.latitude}) AS a, point({x: other.longitude, y: other.latitude}) AS b, other " +
                "WHERE point.distance(a,b) <= %d AND NOT other.uid = '%s' " +
                "RETURN other.uid AS uid, other{.longitude, .latitude, .street} AS loc";
        query = String.format(query, uid, radius, uid);
        return this.session.run(query);
    }

    public Result getShortestRoute(String driverUid, String passengerUid) {
        String query = "MATCH (driver: user {uid: '%s', is_driver: true}), (passenger: user {uid: '%s'}) " +
                "CALL { WITH driver, passenger " +
                "MATCH (s: road {name: driver.street}), (e: road {name: passenger.street}), (s)-[r:ROUTE_TO*]->(e) " +
                "WITH min([i in r | i.travel_time]) AS times " +
                "CALL { WITH times MATCH path = (s)-[r:ROUTE_TO*]->(e) WHERE [i in r | i.travel_time] = times RETURN path } " +
                "UNWIND toIntegerList(times) as x WITH x, path, [i in RANGE(0, length(path)) | reduce(total = 0, j in RANGE(0, i-1) | times[j])] as t " +
                "RETURN sum(x) as total, [i in RANGE(0, length(path)) | {street: nodes(path)[i].name, time: t[i], is_traffic: nodes(path)[i].has_traffic}] as route " +
                "} RETURN {total_time: total, route: route} as data";
        query = String.format(query, driverUid, passengerUid);
        return this.session.run(query);
    }

    public Result addUser(String uid, boolean is_driver) {
        String query = "CREATE (n: user {uid: '%s', is_driver: %b, longitude: 0, latitude: 0, street: ''}) RETURN n";
        query = String.format(query, uid, is_driver);
        return this.session.run(query);
    }

    public Result deleteUser(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) DETACH DELETE n RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserLocationByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n.longitude, n.latitude, n.street";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result updateUserIsDriver(String uid, boolean isDriver) {
        String query = "MATCH (n:user {uid: '%s'}) SET n.is_driver = %b RETURN n";
        query = String.format(query, uid, isDriver);
        return this.session.run(query);
    }

    public Result updateUserLocation(String uid, double longitude, double latitude, String street) {
        String query = "MATCH(n: user {uid: '%s'}) SET n.longitude = %f, n.latitude = %f, n.street = \"%s\" RETURN n";
        query = String.format(query, uid, longitude, latitude, street);
        return this.session.run(query);
    }

    public Result getRoad(String roadName) {
        String query = "MATCH (n :road) where n.name='%s' RETURN n";
        query = String.format(query, roadName);
        return this.session.run(query);
    }

    public Result createRoad(String roadName, boolean has_traffic) {
        String query = "CREATE (n: road {name: '%s', has_traffic: %b}) RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result updateRoad(String roadName, boolean has_traffic) {
        String query = "MATCH (n:road {name: '%s'}) SET n.has_traffic = %b RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result createRoute(String roadname1, String roadname2, int travel_time, boolean has_traffic) {
        String query = "MATCH (r1:road {name: '%s'}), (r2:road {name: '%s'}) CREATE (r1) -[r:ROUTE_TO {travel_time: %d, has_traffic: %b}]->(r2) RETURN type(r)";
        query = String.format(query, roadname1, roadname2, travel_time, has_traffic);
        return this.session.run(query);
    }

    public Result deleteRoute(String roadname1, String roadname2) {
        String query = "MATCH (r1:road {name: '%s'})-[r:ROUTE_TO]->(r2:road {name: '%s'}) DELETE r RETURN COUNT(r) AS numDeletedRoutes";
        query = String.format(query, roadname1, roadname2);
        return this.session.run(query);
    }
} 