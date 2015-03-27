#!/bin/sh

function usage() {
    echo "Usage: terra [plan|apply]"
}

if [ "X$1" == "Xplan" -o "X$1" == "Xapply" ]; then
    terraform $1 -var-file '~/.aws/chriscap1.tfvars' -var-file='values.tfvar' -var 'app_server_ami=ami-f5c6b6cf'
else
    usage
fi