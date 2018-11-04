# yandex-tank-ammo-generator
Convenient ammo generator for yandex tank. General purpose is to generate ammo for load testing of 2018-highload-kv.

<code>mvn package</code>

<code>java -jar ammo-generator-1.0-jar-with-dependencies.jar %MODE% %DIST_FOLDER% [%REQUESTS_TYPE% %URI% %AMOUNT% [%REPLICAS%]]</code>

Restrictions:

    %MODE%:
    
      split - splits all requests into different files by replicas parameter and request type
      
      combine - splits all requests into different files by request type

    %REQUESTS_TYPE%:
    
      GET - special sequence for GET ONLY requests
      
      PUT - special sequence for PUT ONLY requests
      
      MIX - special sequence for MIXED GET/PUT requests

    %AMOUNT%:
  
      for GET >= 3
    
      for PUT >= 2
    
      for MIX >= 10
    
Example: <code>java -jar target/ammo-generator-1.0-jar-with-dependencies.jar combine /home/user GET /v0/entity 1000 get-ammo replicas=2/3 replicas=3/3 PUT /v0/entity 100 put-ammo replicas=2/3 MIX /v0/entity 1000 mix-ammo replicas=2/3 replicas=3/3</code>
