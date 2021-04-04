FROM amazon/aws-cli

RUN yum install jq -y

COPY ../../../../bin/deploy-aws.sh /deploy-aws.sh

ENTRYPOINT ["/bin/bash", "-c"]
