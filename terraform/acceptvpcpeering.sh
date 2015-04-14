#!/bin/sh

echo "The following will show a list of VPC connections in pending state"
PENDING_CONNS=$(aws --profile default ec2 describe-vpc-peering-connections --filter 'Name=status-code,Values=pending-acceptance')

echo $PENDING_CONNS
echo

PCX_ID=$(echo $PENDING_CONNS | sed -e 's/.*\(pcx-........\).*/\1/')


read -p "Accept VPC Peering Request with $PCX_ID [y/N]:" CONTINUE

if [ X${CONTINUE} == "XY" ]; then
    aws ec2 accept-vpc-peering-connection --no-dry-run --vpc-peering-connection-id ${PCX_ID}

else
    echo "Only doing a dry run of the command to check likely success"
    echo
    aws ec2 accept-vpc-peering-connection --dry-run --vpc-peering-connection-id ${PCX_ID}

fi
