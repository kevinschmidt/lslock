#!/bin/bash

if [[ $# -eq 0 ]] ; then
  echo 'Usage: lslock.sh [directory]'
  exit 0
fi

sbt --error "run $1"
