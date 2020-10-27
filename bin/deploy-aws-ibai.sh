#!/bin/bash

env=$1
AWS_ACCESS_KEY_ID=$2
AWS_SECRET_ACCESS_KEY=$3
TASK_DEFINITION=$4

DOCKER_IMAGE=ayudadigital/huelladigital-backend:beta-aws-ibai
CLUSTER_NAME=default
TASK_FAMILY=backend-task
SERVICE_NAME=backend-service-ssm

#TASK_DEFINITION=$(aws ecs describe-task-definition --task-definition ${TASK_DEFINITION_NAME} --region us-east-1)
#TASK_DEFINITION=$(cat ~/Documents/ayudadigital/backend_task_definition_template.json)
echo ${TASK_DEFINITION} | jq '.containerDefinitions[0].image='\"${DOCKER_IMAGE}\"
echo ${TASK_DEFINITION} | jq '.containerDefinitions[0].image='\"${DOCKER_IMAGE}\" > /tmp/new_task_defintion.json
NEW_TASK_DEFINITION=$(aws ecs register-task-definition --family ${TASK_FAMILY} --region us-east-1 --cli-input-json file:///tmp/new_task_defintion.json)
echo "======================================"
echo "New task definition:"
echo "$NEW_TASK_DEFINITION"
echo ""
NEW_TASK_VERSION=$(echo ${NEW_TASK_DEFINITION} | jq --raw-output '.taskDefinition.revision')
echo "New task version: ${NEW_TASK_VERSION}"
echo ""
echo "======================================"
echo "Updating service with new task definition:"
NEW_SERVICE_DEFINITION=$(aws ecs update-service --cluster $CLUSTER_NAME --service $SERVICE_NAME --task-definition $TASK_FAMILY:$NEW_TASK_VERSION)
echo "New service:"
echo $NEW_SERVICE_DEFINITION
echo ""
