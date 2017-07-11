#!/bin/sh

if [ "$1" == "" ]
then
	echo "Usage: deploy server-name [service-name] [env-name]"
	exit 1
fi

INST_FILE=`find . -name "$2*install.jar" -print`
RINST_FILE=`echo $INST_FILE | awk -F '/' '{ print $(NF) }'`

scp $INST_FILE andpay@$1:install/$RINST_FILE

SRV_CMD=`echo $RINST_FILE | awk -F '-install' '{ print $1}'`

SRV_PROC=`ssh -landpay $1 "ps -ef | grep $SRV_CMD | grep -v bash | grep -v grep"`
SRV_PID=`echo $SRV_PROC | awk -F ' ' '{ print $2}'`

if [ "$SRV_PID" != "" ]
then
	ssh -landpay $1 "kill -9 $SRV_PID"
fi

SRV_P_DIR=`echo $SRV_CMD | sed 's/-.*$//'`
SRV_DIR=`echo $SRV_CMD | sed 's/-[0-9].*$//'`

ssh -landpay $1 "mkdir app" 1>/dev/null 2>&1

ssh -landpay $1 "cd app; mkdir $SRV_P_DIR" 1>/dev/null 2>&1

ssh -landpay $1 "cd app; cd $SRV_P_DIR; mkdir $SRV_DIR" 1>/dev/null 2>&1

ENV_NAME="stage0"
if [ "$3" != "" ] ; then
	ENV_NAME=$3
fi

ssh -landpay $1 "cd app/$SRV_P_DIR/$SRV_DIR; rm -rf; jar xvf \$HOME/install/$RINST_FILE; config-tool $ENV_NAME"

SRV_SHELL=`ssh -landpay $1 "cd app/$SRV_P_DIR/$SRV_DIR/bin; ls $SRV_P_DIR*"`

ssh -landpay $1 "cd app/$SRV_P_DIR/$SRV_DIR/bin; chmod a+x *; nohup ./$SRV_SHELL < /dev/null 1>nohup.out 2>&1 &"

ssh -t -x -landpay $1 "cd app/$SRV_P_DIR/$SRV_DIR/bin; tail -f nohup.out"
