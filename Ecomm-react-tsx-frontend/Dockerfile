# Ecomm-react-tsx-frontend/Dockerfile
# Stage 1: Build React TypeScript App
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .
# Vite build
RUN npx vite build

# Stage 2: Production Nginx Server
FROM nginx:1.25-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]