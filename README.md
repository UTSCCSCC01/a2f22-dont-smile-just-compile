# TO RUN THE APPTESTS

All databases should be clear before running tests, and all services should be running in docker. 

The LocationMicroservice tests must be run before the TripMicroservice tests are run.

- tripRequestPass() requires getNearbyDriverPass() from the location app tests

- driverTimePass() requires getNavigationPass() from the location app tests

- tripConfirmPass() requires getNavigationPass() from the location app tests

The tests for TripMicroservice require LocationMicroservice and UserMicroservice to be running in docker.

The endpoints for UserMicroservice assume that Login/Register do **NOT** return the uid

/trip/confirm returns the _id as a field inside the "data" object of the response body. (Ex: { "data": {"_id" : "1"}, "status": "OK" } )

## We assumed that when passed in the request body:

uid, timeElapsed are **strings**

startTime, endTime are **integers**

All other pay and discount parameters are **doubles**

trip \_id is a **VALID** ObjectID hexidecimal. Otherwise it will return an error status code.


