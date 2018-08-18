homeautomation

This is a test implementation of a Nest Thermostat and a Ring camera. This is a beta release.

Getting started:
You will need a user account and devices to use this system.

You can create a Ring user account at:
https://ring.com/users/sign_in
click 'Create a new account'

You can create a Nest user account at:
https://home.nest.com/
click 'Sign up'

The web interface can be accessed at home.html

Software used in development
Eclipse Photon, Tomcat v8.5, and JUnit5

Author
Nikki Peck - beta version

Debts
okhttp-3.11.0.jar used to create Http requests, reqsponses and clients for get/put/post calls. Handles connection
resets and redirects (most of the time).
okio-2.0.0-RC1.jar okhttp-3.11.0.jar depends on this
annotations-13.0.jar method annotations that okhttp-3.11.0.jar depends on
javax.json-1.1.jar and javax.json-api-1.0.jar for handling of json strings, objects and arrays
taglibs-standard-impl-1.2.5.jar and taglibs-standard-spec-1.2.5.jar for jsp tags

Acknowledgments 
Hats off to davglass/doorbot, the javascript Ring API allowed me to get started with my own implementation
