#!/bin/sh

function usage() {
    echo "Usage: terra [get|plan|apply|destroy]"
}

if [ "X$1" == "Xget" -o "X$1" == "Xplan" -o "X$1" == "Xapply" -o "X$1" == "Xdestroy" ]; then
    terraform $1 -var-file '~/.aws/childcap.tfvars' -var-file='values.tfvar' -var 'app_server_ami=ami-0b235e31' ${@:2}
else
    usage
fi
