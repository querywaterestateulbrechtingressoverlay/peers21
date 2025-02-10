FROM nginx:latest
COPY default.conf /etc/nginx/conf.d/default.conf
COPY frontend/ /usr/share/nginx/html/
RUN chmod 777 /usr/share/nginx/html/index.html
RUN chmod 777 /usr/share/nginx/html/css/styles.css
RUN chmod 777 /usr/share/nginx/html/js/peer-api.js
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
