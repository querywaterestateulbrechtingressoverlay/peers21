cd backend/data-layer/src/main/resources && openssl genrsa -out private.pem 2048 && openssl rsa -in private.pem -pubout -out public.pem
cd ../../.. && ./gradlew build -x test
cd ../webscraper/ && ./gradlew build -x test
cd ../../docker && docker compose up --build
