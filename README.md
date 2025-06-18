# Launch of external services for the capstone project

### Prerequisites

Install [Docker](https://docs.docker.com/desktop/mac/install/) and [Docker Compose](https://docs.docker.com/compose/install/).

### Instruction

1. Go to directory with `docker-compose.yml`.
    - `cd {PROJECT_DIR}`
    
2. Build and run apps with Docker Compose.
    - `docker-compose up --build`
    
3. Stop **Product Info service** _(for testing)_
    - `docker-compose stop product-info-service`
    
4. Start **Product Info service** _(for testing)_
    - `docker-compose start product-info-service`

5. Start **Order Aggregator service** _(for testing)_
    - `docker-compose start order-aggregator-service`

Project Checklist:

✅ Step 1 & 2: Launch external services: did this perfectly with docker-compose.

✅ Step 3: Create a Spring Boot application: I have created a well-structured, `order-aggregator-service` professionally named project.

✅ Step 4: Implement UserInfoRepository: service correctly fetches users from MongoDB. The log line Found user: John proves this.

✅ Step 5: Create integrations: I have successfully integrated with both the order-search-service and the product-info-service using WebClient.

✅ Step 6: Prepare aggregation logic: service correctly combines data from all three sources (MongoDB, Order Search, Product Info).

✅ Step 7: Implemented REST API in multi-value stream format: The curl output shows a stream of ndjson objects, which is exactly what was required. `curl http://localhost:8083/api/orders/user2`

✅ Step 9: Launch and test the application:

    ✅ Make a call to your endpoint: I did this with curl.

    ✅ Check that the service aggregates all required data: The output with Apple and Meal for product names shows this is working.

✅ Make sure that logs contain all required information: logs clearly show the process.