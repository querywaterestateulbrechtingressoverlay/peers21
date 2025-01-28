docker run -p 5432:5432 --name postgres-db -e POSTGRES_USER=$DB_USERNAME -e POSTGRES_PASSWORD=$DB_PASSWORD -d postgres
cd demo && ./gradlew bootRun
