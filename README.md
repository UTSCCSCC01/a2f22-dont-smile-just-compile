# TO RUN THE APPTESTS

All databases should be clear before running tests, and all services should be running in docker. 

The tests for TripMicroservice require LocationMicroservice to be running in docker.

The endpoints for UserMicroservice assume that Login/Register do **NOT** return the created \_id

## We assumed that when passed in the request body:

uid, timeElapsed are **strings**

startTime, endTime are **integers**

All other pay and discount parameters are **doubles**

trip \_id is a **VALID** ObjectID hexidecimal.


