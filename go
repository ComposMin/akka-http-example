#!/usr/bin/env bash

function usage() {
  echo './go [build|validate|publish|deploy]'
}

# ensure SBT is installed

# run build task

if [ X$1 == 'Xbuild' ]; then
  sbt assembly

elif [ X$1 == 'Xpackage' ]; then
  cd packer
  packer build --debug -var 'source_ami=ami-fd9cecc7'  -var 'subnet_id=subnet-cb18ccae' -var 'security_group_id=sg-e1b91c84' akkabox.json

elif [ X$1 == 'Xvalidate' ]; then
  cd packer
  packer validate -var 'source_ami=ami-fd9cecc7' -var 'subnet_id=subnet-cb18ccae' -var 'security_group_id=sg-e1b91c84' akkabox.json

elif [ X$1 == 'Xpublish' ]; then
  echo TODO: publish

elif [ X$1 == 'Xdeploy' ]; then
  echo TODO: deploy
else
  usage
fi
