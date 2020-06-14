# Prepare build assets
FROM node:12.18.0-alpine3.9 AS builder
ARG BUILD_ID=local
LABEL stage=builder
LABEL BUILD_ID=$BUILD_ID
WORKDIR /workspace
COPY . /workspace/
RUN npm install
RUN npm run build
# FIXME: To refactor when the frontend uses dinamic endpoint through variables instead static "localhost" endpoint
RUN sed -i "s#http://localhost:8080#http://backend:8080#g" $(grep -r -l "http://localhost:8080" *)

# Serve build
FROM node:12.18.0-alpine3.9
RUN npm install -g serve
WORKDIR /workspace
COPY --from=builder workspace/build /workspace/build/
EXPOSE 5000
ENTRYPOINT ["serve", "-s", "build"]
