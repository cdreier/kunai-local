# kunai-local

a few months ago, i released a small android app named ["Kunai - Remote Keyboard"](http://kunai-keyboard.net/), where you can connect your android device with your browser. Everything you type in the browser is send immediately to the device. This is awesome and so simple!

Till now, your phone had to connect to my server with my choosen ports etc. 
To run Kunai locally without the needs of a connection to the world wide web, you can now use this sever. Just start the jar file and a browser will open with everything you need to type on your phone in your local network.

Please find detailed informations on the [Kunai Website](http://kunai-keyboard.net/local.php)

## maven build

to build the server yourself, you only need [maven](http://maven.apache.org/) installed. Clone the kunai-local repository and run 

````
mvn clean package
````

After your build is (hopefully) successful, you will find an all-in jar file in the kunai-local/target folder.




