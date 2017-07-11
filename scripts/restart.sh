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

SRV_P_DIR=`echo $SRV_NAME | sed 's/-.*$//'`
SRV_DIR=$SRV_NAME

SRV_SHELL=`ssh -landpay $1 "cd app/$SRV_P_DIR/$SRV_DIR/bin; ls $SRV_P_DIR*"`

ssh -landpay $1 "cd app/$SRV_P_DIR/$SRV_DIR/bin; chmod a+x *; nohup ./$SRV_SHELL < /dev/null 1>nohup.out 2>&1 &"

ssh -t -x -landpay $1 "cd app/$SRV_P_DIR/$SRV_DIR/bin; tail -f nohup.out"
