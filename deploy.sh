cd backend/data-layer/ && ./gradlew build -x test
cd ../webscraper/ && ./gradlew build -x test
cd ../../docker && docker compose up --build
