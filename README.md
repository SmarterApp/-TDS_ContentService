# TDS Content Service
## Overview
The `TDS_ContentService` (Content Support Service) is a Java 8/Spring Boot microservice used for fetching content (item metadata) and media 
resources. 

### Optional implementations
Currently, there are two supported implementations for item/resource retrieval:

1. Fetching from S3 (using the `s3Content` Spring profile)
2. Fetching from disk (within the tds-content-service container)

It is highly recommended to use S3 as the item repository due to the volume of item content and resource data, as the data must 
be packaged within the content service docker container otherwise.

### Encryption
ContentService supports base64 encryption for item/resource paths. This option should be enabled only if the TDS_Student 
app is configued to support encryption. Additionally, the same encryption key **must** be used.

To enable encryption, configure the following properties:

 - tds.encryption-enable : set to "true"
 - tds.encryption-key : set to the same value as what is configured in TDS/ProgMan
 
### Docker Support
The Student Support Service provides a `Dockerfile` for building a Docker image and a `docker-compose.yml` for running a Docker container that hosts the service `.jar`.  For the following command to work, the Docker Engine must be installed on the target build machine.  Resources for downloading and installing the Docker Engine on various operating systems can be found [here](https://docs.docker.com/engine/installation/).  For details on what Docker is and how it works, refer to [this page](https://www.docker.com/what-docker).

To build the service and its associated Docker image:

* `mvn clean install docker:build`
