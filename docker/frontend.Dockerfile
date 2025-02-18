FROM nginx:latest
COPY /docker/default.conf /etc/nginx/conf.d/default.conf
COPY frontend/ /usr/share/nginx/html/
RUN chmod -R +rx /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
