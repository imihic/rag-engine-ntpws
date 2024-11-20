# RAG (Retrieval-Augmented Generation) Engine

This project is a simple web application that leverages the power of RAG (Retrieval-Augmented Generation) models to generate text based on user uploaded documents. 
The application is built using the [Hilla](https://hilla.dev) framework, which is a full-stack Java framework for building modern web applications.

For RAG tasks, the project uses the ONNX runtime to run the embedding models and uses Langchain4j to communicate with generation large language models either hosted locally or on the cloud (e.g. OpenAI API).


## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.
You can also import the project to your IDE of choice as you would with any
Maven project.

## Deploying to Production

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/myapp-1.0-SNAPSHOT.jar` (NOTE, replace
`myapp-1.0-SNAPSHOT.jar` with the name of your jar).

## Deploying using Docker

To build the Dockerized version of the project, run

```
mvn clean package -Pproduction
docker build . -t rag-engine:latest
```

Once the Docker image is correctly built, you can test it locally using

```
docker run -p 8080:8080 rag-engine:latest
```
