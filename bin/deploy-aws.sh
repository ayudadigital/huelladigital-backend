#!/bin/bash

env=$1
DOCKER_TAG=$2

DOCKER_IMAGE=ayudadigital/huelladigital-backend:${DOCKER_TAG}
CLUSTER_NAME="${env}-ecs-cluster"
TASK_FAMILY="${env}-huellapositiva-backend-td"
SERVICE_NAME=huellapositiva-backend-ecs-service

TASK_DEFINITION=$(aws ecs describe-task-definition --task-definition "${TASK_FAMILY}" --region "us-east-1")
echo "======================================"
echo "Current task definition:"
echo "${TASK_DEFINITION}"
echo "======================================"
NEW_TASK_DEFINITION=$(echo "$TASK_DEFINITION" | jq --arg IMAGE "$DOCKER_IMAGE" '.taskDefinition | .containerDefinitions[0].image = $IMAGE | del(.taskDefinitionArn) | del(.revision) | del(.status) | del(.requiresAttributes) | del(.compatibilities) | del(.registeredAt) | del(.registeredBy)')
echo "New task definition:"
echo "$NEW_TASK_DEFINITION"
echo "======================================"
NEW_TASK_DEFINITION_RESULT=$(aws ecs register-task-definition --region "us-east-1" --cli-input-json "$NEW_TASK_DEFINITION")
echo "New task definition:"
echo "${NEW_TASK_DEFINITION_RESULT}"
echo ""
NEW_TASK_VERSION=$(echo "${NEW_TASK_DEFINITION_RESULT}" | jq --raw-output '.taskDefinition.revision')
echo "New task version: ${NEW_TASK_VERSION}"
echo ""
echo "======================================"
echo "Updating service with new task definition."
echo "New service:"
aws ecs update-service --cluster "$CLUSTER_NAME" --service $SERVICE_NAME --task-definition "$TASK_FAMILY":"$NEW_TASK_VERSION" --region us-east-1
