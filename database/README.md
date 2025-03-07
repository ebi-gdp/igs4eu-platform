# Database Setup Instructions

## Overview

The detailed database setup documentation is available on [Confluence](https://www.ebi.ac.uk/seqdb/confluence/x/oI9EE). Please refer to it for step-by-step instructions on creating and configuring the database on **Google Cloud Platform (GCP)**.

Once the database is set up on GCP, execute the following SQL scripts to complete the schema setup.

## Current database setup

| Environment       | Database Name    | Schema name             | Comments                                                                 |
|-------------------|------------------|-------------------------|--------------------------------------------------------------------------|
| Development (dev) | `intervene`      | `intervene-dev`         |                                                                          |
| Production (prod) | `intervene-prod` | `geneticscoresplatform` |                                                                          |  
| Testing (test)    | `intervene-test` | `geneticscoresplatform` | Not yet setup, once done update this details <br/> if there any changes. |

Feel free to change database name but make sure you need to change this details in respective microservices config so they can connect to the Database.

## Database initialization scripts

Once you have **PostgreSQL** database instance up & running, database & schema has been created as mentioned above then 
set up the PostgreSQL database, execute the following SQL files in sequence

1. **`intervene-1.sql`**
    - Defines data types (enums)
    - Sets up sequences
    - Defines stored procedures

2. **`intervene-2.sql`**
    - Creates necessary tables
    - Establishes additional constraints

> **Note:** Run the commands in each SQL file one after another to ensure proper database setup.

## Additional Information

- Ensure you have the correct permissions to execute these scripts.
- Verify that the database connection is properly configured before running the scripts.
- Modify configurations as per the environment (Development, Test, or Production).

# Note
There are diff. approaches to handle installation of database scripts, one of the options can be `Flyway`.
Current setup is simple, in the future such strategy can be adopted!
