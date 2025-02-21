FROM amazoncorretto:23.0.2-alpine3.21
ARG JAR_FILE=build/libs/*.jar

ARG API_USERNAME=user
ARG API_PASSWORD=password
ARG API_ENDPOINT=http://endpoint
ARG TOKEN_ENDPOINT=http://t-endpoint
ARG INTERNAL_API_ENDPOINT=http://b-endpoint
ARG INTERNAL_API_USERNAME=buser
ARG INTERNAL_API_PASSWORD=bassword

ENV API_USERNAME=${API_USERNAME}
ENV API_PASSWORD=${API_PASSWORD}
ENV API_ENDPOINT=${API_BASE_ENDPOINT}
ENV TOKEN_ENDPOINT=${API_TOKEN_ENDPOINT}
ENV INTERNAL_API_ENDPOINT=${INTERNAL_API_ENDPOINT}
ENV INTERNAL_API_USER=${INTERNAL_API_USERNAME}
ENV INTERNAL_API_PASSWORD=${INTERNAL_API_PASSWORD}

COPY ${JAR_FILE} app.jar
RUN echo https://repo.jing.rocks/alpine/v3.21/community >> /etc/apk/repositories \
    echo https://repo.jing.rocks/alpine/v3.21/main >> /etc/apk/repositories \
    && apk add firefox
ENTRYPOINT ["java", "-jar", "app.jar"]
