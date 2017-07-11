#!/bin/sh

if [ "$#" -lt "2" ]
then
	echo "Usage: restart server-name service"
	exit 1
fi

SRV_NAME=$2

SRV_PROC=`ssh -landpay $1 "ps -ef | grep $SRV_NAME | grep -v bash | grep -v grep"`
SRV_PID=`echo $SRV_PROC | awk -F ' ' '{ print $2}'`

if [ "$SRV_PID" != "" ]
then
	ssh -landpay $1 "kill -9 $SRV_PID"
fi
