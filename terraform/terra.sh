#!/bin/sh

function usage() {
    echo "Usage: terra [plan|apply|destroy]"
}

if [ "X$1" == "Xplan" -o "X$1" == "Xapply" -o "X$1" == "Xdestroy" ]; then
    terraform $1 -var-file '~/.aws/chriscap1.tfvars' -var-file='values.tfvar' -var 'app_server_ami=ami-bf403385' ${@:2}
else
    usage
fi
