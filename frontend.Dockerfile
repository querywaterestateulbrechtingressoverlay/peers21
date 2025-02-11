FROM nginx:latest
COPY default.conf /etc/nginx/conf.d/default.conf
COPY frontend/ /usr/share/nginx/html/
RUN chmod 644 /usr/share/nginx/html/index.html
RUN chmod 644 /usr/share/nginx/html/css/styles.css
RUN chmod 644 /usr/share/nginx/html/js/peer-api.js
RUN chmod -R +rx /usr/share/nginx/html/css
RUN chmod -R +rx /usr/share/nginx/html/js
RUN chmod -R +rx /usr/share/nginx/html/css/styles.css
RUN chmod -R +rx /usr/share/nginx/html/js/peer-api.js
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
