#!/bin/sh

if [ "$#" -lt "2" ]
then
	echo "Usage: status server-name service"
	exit 1
fi

SRV_NAME=$2

SRV_PROC=`ssh -landpay $1 "ps -ef | grep $SRV_NAME | grep -v bash | grep -v grep"`

echo $SRV_PROC
