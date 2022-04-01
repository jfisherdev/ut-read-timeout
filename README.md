# ut-read-timeout
Test project for investigating an issue with read timeouts being inappropriately triggered in Undertow.

In some cases I have observed the read timeout being triggered on a POST request not in response to an actual read operation 
exceeding the timeout but because the call/request processing time exceeds the read timeout.

This project produces two war applications:

The first is message-server.war, which contains the endpoint /services/slowpost/generate-random that takes a POST JSON request in this format:
```
{
    "messageLength": <message-length, 16 by default>
    "timeUnit": <time-unit, MILLISECONDS by default>,
    "waitTime": <wait-time, 0 by default>
}
```

This will pause for the specified amount of time before generating a random string message and then sending it back in 
the response. 

This is likely the main one needed for testing.

There is a second one called messaging-webclient.war that can be used to send requests to the previously mentioned endpoint
but either as a GET or POST request, with request parameters that match the names shown in the JSON above. 
I am not sure if this is necessary, but it happened to match the original application structure where I originally 
observed this.

Log messages are placed throughout these applications to show progress, and in the case of message-server.war that even
though the request processing can be slow it is NOT reading POST body data.

Running the deployStandalone task will deploy the WARs to the standalone/deployments directory of a WildFly server, but 
this application can be deployed however you see fit for testing.

Recreating this issue is NOT as simple as choosing a waitTime that exceeds the configured read timeout, though that is 
part of it. It seems that there may also need to be concurrent requests with different wait/processing times being made 
as well. 

