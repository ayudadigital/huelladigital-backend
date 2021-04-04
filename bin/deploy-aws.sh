#!/bin/bash

CLUSTER_NAME=$1
SERVICE_NAME=$2
TASK_FAMILY=$3
DOCKER_IMAGE=$4
AWS_ACCESS_KEY_ID=$5
AWS_ACCESS_KEY_SECRET=$6
export AWS_ACCESS_KEY_ID AWS_ACCESS_KEY_SECRET

echo "Cluster name: $CLUSTER_NAME"
echo "Service name: $SERVICE_NAME"
echo "Task family: $TASK_FAMILY"
echo "Docker image: $DOCKER_IMAGE"
aws --version

#TASK_DEFINITION=$(aws ecs describe-task-definition --task-definition "${TASK_FAMILY}" --region "us-east-1")
#echo "======================================"
#echo "Current task definition:"
#echo "${TASK_DEFINITION}"
#echo "======================================"
#NEW_TASK_DEFINITION=$(echo "$TASK_DEFINITION" | jq --arg IMAGE "$DOCKER_IMAGE" '.taskDefinition | .containerDefinitions[0].image = $IMAGE | del(.taskDefinitionArn) | del(.revision) | del(.status) | del(.requiresAttributes) | del(.compatibilities) | del(.registeredAt) | del(.registeredBy)')
#echo "New task definition:"
#echo "$NEW_TASK_DEFINITION"
#echo "======================================"
#NEW_TASK_DEFINITION_RESULT=$(aws ecs register-task-definition --region "us-east-1" --cli-input-json "$NEW_TASK_DEFINITION")
#echo "New task definition:"
#echo "${NEW_TASK_DEFINITION_RESULT}"
#echo ""
#NEW_TASK_VERSION=$(echo "${NEW_TASK_DEFINITION_RESULT}" | jq --raw-output '.taskDefinition.revision')
#echo "New task version: ${NEW_TASK_VERSION}"
#echo ""
#echo "======================================"
#echo "Updating service with new task definition."
#echo "New service:"
#aws ecs update-service --cluster "$CLUSTER_NAME" --service $SERVICE_NAME --task-definition "$TASK_FAMILY":"$NEW_TASK_VERSION" --region us-east-1
