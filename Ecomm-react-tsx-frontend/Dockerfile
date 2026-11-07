# Ecomm-react-tsx-frontend/Dockerfile
# Stage 1: Build React TypeScript App
FROM node:24-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
# Vite build
RUN npx vite build

# Stage 2: Production Nginx Server
# Stage 2: Production Nginx Server (Updated to latest stable tag with security patches)
FROM nginx:1.27-alpine

# Update Alpine system packages to patch CVE vulnerabilities
RUN apk update && apk upgrade --no-cache
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]