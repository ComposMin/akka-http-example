#!/bin/sh


echo "The following details include the VPC ID of this accounts VPC to use for the peering connection"
aws  --profile childcapuser ec2 describe-vpcs --output text

echo

# These values should be fairly static, but vary depending on the CorpName vpc used per test environment
PEER_VPC_ID=vpc-fcb04999
PEER_OWNER_ID=911868327851

echo "Dry Run of Establishing a VPC peering connection"
aws --profile childcapuser ec2 create-vpc-peering-connection --dry-run --peer-vpc-id ${PEER_VPC_ID} --peer-owner-id ${PEER_OWNER_ID} --vpc-id vpc-4f7bb52a

read -p "Continue with VPC Request [Y/n]:" CONTINUE_CREATE

if [ X${CONTINUE_CREATE} == "XY" ]; then
    echo "Really Establishing a VPC peering connection"
    aws --profile childcapuser ec2 create-vpc-peering-connection --no-dry-run --peer-vpc-id ${PEER_VPC_ID} --peer-owner-id ${PEER_OWNER_ID} --vpc-id vpc-4f7bb52a
else
    echo "Did not execute creation"
fi
