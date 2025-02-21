FROM nginx:latest
COPY default.conf /etc/nginx/conf.d/default.conf
COPY files/ /usr/share/nginx/html/
RUN chmod -R +rx /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
