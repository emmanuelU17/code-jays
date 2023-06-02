# stage 1
FROM node:latest as builder
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build --configuration=production


# stage 2
FROM nginx:alpine
COPY --from=builder /app/dist/frontend-angular /usr/share/nginx/html
