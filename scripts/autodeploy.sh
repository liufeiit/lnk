#!/bin/bash

#before execute this shell, make sure first use the sshcfg.sh to config the ssh login without password for the user
#this shell is designed for auto-deploy local maven build file to the remote destination
#it will do the following steps
#	1. check environment like group/username/ssl (if username not provide, use andpay as default value)
#	2. create relative dir and copy config files
#	3. copy build files and start app

#   the most common situation of this tool usage is 

#	0). config  auto ssl login to remote host (default user is andpay)
#		./autodeploy.sh -m sslcfg -h hostname 
#		./autodeploy.sh -m sslcfg -s serviceName

#	1). account and ssl is well configured, just need to deploy to remote host
#		./autodeploy.sh -m deploy -s ti-quartz-srv 

#   2). prepare env(create account, ssl config). this is done by user who have the root account. this is done by admin user
		
#		./autodeploy.sh  -m init -u username -h host

#   3. show remote log

#      ./autodeploy.sh -s serviceName -m showlog

#	4). the remote host is completely a new machine, then we need to create account. the tool will config ssl automatically for you
		
#		./autodeploy.sh -m deployall

# the following parameters need read from input

username=andpay
group=andpay

admin=root
sshpub=~/.ssh/id_dsa.pub

HOST_CFG_PROP_FILE=hostcfg.properties
WORKSPACE_PATH=~/work

APPTOOL_NAME=app-tools
DEPLOY_SHELL=deploy.sh

#the order of services to be deployed
allJars=(bgw-common-srv bgw-payment-srv)
allWars=(bgw-payment-api-web bgw-payment-notify-web bgw-payment-web)

#user,group, host
init(){
	
    local uname=$1
	local remotehost=$2    
    if [ -z $remotehost ];then
		echo "Can't find which host to connect."
		exit 1
	fi
    
    #create user account
    preaccount $uname $remotehost
    
    #check ssh login without pass is ok
	checkSSL $uname $remotehost 
	
	if [ $? != 0 ]; then
		echo "SSL auto login is not ok,try to config ssl auto login..."
        sslcfg $uname $remotehost
	fi
    
    #copy app-tools and deploy.sh and configuration files
 
    local ruserhome=`ssh $admin@$remotehost "echo ~$uname"`
    
    remotecopy $admin $remotehost "/usr/local/bin/$APPTOOL_NAME"
    
    remotecopy $uname $remotehost "$ruserhome/$DEPLOY_SHElL"
    
    #remotecopy $uname $remotehost "$ruserhome/.ti_config"
    
    remotecopy $uname $remotehost "$ruserhome/.ns_config"
    
    remotecopy $uname $remotehost "$ruserhome/.publish_config"
    
	ssh $admin@$remotehost "
			mkdir -p /data/applog
			mkdir -p /data/ftplog
			chown -R $uname /data
			ln -s /data/applog /log >> /dev/null 2>&1"
    
	ssh $uname@$remotehost "
			cd ~
			mkdir -p app/ac
			mkdir -p app/af
			mkdir -p app/ti"   
            
	echo "Prepare env done!"
}

buildproject(){
	local pdir=$1
    
    if [ ! -d $pdir ]; then
        echo "Error: project directy $pdir is not existed!"
        exit 1
    fi
    
    cd $pdir
	mvn clean install -Dmaven.test.skip=true | tee build.log
	local success=`grep "BUILD SUCCESS" build.log  > /dev/null 2>&1`
	if [ $? == 0 ]; then
		rm -rf build.log 
		cd -
	else
		echo "Build failed, view $pdir/build.log for more details"
		cd -
		exit -1
	fi
}

# create relative account is not existed
preaccount(){
    local uname=$1
    local remotehost=$2
    
	checkgroup $group $remotehost
	if [ $? == 0 ]; then
		echo "Check group done! "
	else
		ssh $admin@$remotehost "groupadd $group"
		echo "Group $group is created"
	fi
    
	checkuser $uname $remotehost
    
	if [ $? != 0 ]; then
		echo "Try to create user $uname"
		ssh $admin@$remotehost "useradd -d /home/$uname -g $group $uname
        mkdir -p /home/$uname
        chown -R $uname /home/$uname
        passwd $uname"        
        echo "Create user done! "
    fi
}

# check user name is existed, this is done by admin, a user can't check itself
checkuser(){
	#ssh $admin@$2 "egrep -i ^$2 /etc/passwd && echo $?" |grep $1 > /dev/null 2>&1
    #local result=`ssh $admin@$2 "egrep -i ^$2 /etc/passwd"`
    ssh $admin@$2 "cat /etc/passwd" | egrep -i "^$1:"  > /dev/null 2>&1
	return $?
}

checkgroup(){
	ssh $admin@$2 "cat /etc/group" | egrep -i "^$1:" > /dev/null 2>&1
	return $?
}

remotecopy(){
    local user=$1
    local rhost=$2
    local targetfile=$3
    local fname=`echo $targetfile | rev | cut -d/ -f1 | rev`
    ssh $user@$rhost "test -e $targetfile"
    if [ $? != 0 ]; then
        if [ -f $workspace/$fname -o -d $workspace/$fname ]; then
            scp -r $workspace/$fname $user@$remotehost:$targetfile
            echo "Copy file $fname done!"
        else
            echo "Error: $workspace/$fname is not existed, can't init the environment"
            exit 1
        fi
    else
        echo "Skip copy file $fname "
    fi
}

#show remote log
showlog(){
    remotehost=`cat $HOST_CFG_PROP_FILE | grep "$2" | cut -d'=' -f2`   
    #shortName=`echo $service|cut -d'-' -f 1-2`
    logdirs=(`ssh $1@$remotehost "find /log/java -name $service"`)  
    
    if [ ${#logdirs[@]} == 0 ]; then
        echo "Can't find log!"
        exit 1
    else
        #echo "aaaa${logdirs[0]}"
        logdir=${logdirs[0]}
        fname=`echo ${logdir##*/}`
        logfile="$logdir/log/$fname.log"
        ssh $1@$remotehost "less $logfile" | less
    fi
    echo "$logfile"
}

checkSSL(){
    local uname=$1
    local remotehost=$2
	local userhome=`ssh $admin@$remotehost "echo ~$uname"`
    
    local aukeys=`ssh $admin@$remotehost "cat $userhome/.ssh/authorized_keys"`
    
    if [ -z "$aukeys" ]; then
        return 1
    else
        local pubcontent=`cat $sshpub`
        local result=`echo aukeys | grep "$pubcontent"`
        return $result
    fi
}

#ssl auto login configuration
sslcfg(){
    local uname=$1
	local remotehost=$2
    if [ -z $remotehost ];then
		echo "Can't find which host to connect."
		exit 1
	fi
    
	if [ -f "$sshpub" ]; then
		echo "Try to add $sshpub it to remote host $remotehost"     
        local pubcontent=`cat $sshpub`
        ssh $uname@$remotehost "mkdir -p ~/.ssh && echo $pubcontent >> ~/.ssh/authorized_keys"
	else
		echo "$sshpub not existed,will generate pub file!";
		cd ~
		ssh-keygen -t dsa
		cd -
		sslcfg $uname $remotehost
	fi
    
    echo "SSL config success!"
}

#deploy a single service
deploy(){

	local sname=$1
    local remotehost=$2
    local uname=$3
    local rdir=`findProjectDir $sname`
    local pdir="$workspace/$rdir"
    
	buildproject $pdir
    
    stype=jar
    local wi=`findWarInfo $sname`
    if [ ! -z $wi ]; then
        stype=war
        file=`ls $pdir/target/$sname*SNAPSHOT.war`
    else
        file=`ls $pdir/target/$sname*SNAPSHOT-install.jar`
    fi
 
	#copy the jar file to remote host
    
    if [ ! -f $file ]; then 
        echo "Error: Can't find build target file!"
        exit 1
    fi
    
	#local file=$pdir/target/$jarfilename
	
	#projectname=`echo ${pdir##*/}`
	#searchstring="-"
	#pos=`echo $projectname | sed -n "s/[$searchstring].*//p" | wc -c`
	#projectprefix=`echo ${string1:0:$pos-1}`
	
	#targetdir=app/$projectprefix/

	local userhome=`ssh $uname@$remotehost "echo ~"`
	local targetdir=$userhome/install/
	local fname=`basename $file`
    local targetfilepath=$targetdir$fname
    local dstr=`date '+%Y%m%d%H%M%S'`
    local backup=$targetfilepath.$dstr.bak
    
    echo "backup..."
    ssh -tt $uname@$remotehost "mv $targetfilepath $backup"
    
	echo "Try to copy file $file to $remotehost:$targetdir"

	scp $file $uname@$remotehost:$targetdir
	echo "Copy file $file done! try to deploy project...."
	
	ssh -tt $uname@$remotehost "
		./$DEPLOY_SHELL $stype $sname
	"
}

findWarInfo() {
        for wi in ${allWars[@]}
        do
                if [ "$1" == "${wi%%|*}" ]
                then
                        echo $wi
                fi
        done
}

#find the project build dir relative to the workspace
findProjectDir(){    
    local wi=`findWarInfo $1`
    if [ -z $wi ]; then 
        #it's not war service
        local prefix=`echo $1 | cut -d'-' -f 1-2`
        echo "$prefix/$1"
    elif [ $wi == "af-cfc-yeepay-web" ]; then
        echo "af-cfc-yeepay/af-cfc-yeepay/af-cfc-yeepay-web"
    else
        echo $1
    fi
}

#deploy all the project sequencely
deployall(){
    local local_array=("${@}")
	for ser in "${local_array[@]}"
	do
        #different project have different host but default the same user
        local lhost=`cat $HOST_CFG_PROP_FILE | grep "${ser}" | cut -d'=' -f2`
        echo -e "Deploy ${ser} --> $lhost\n"
		deploy ${ser} $lhost $username    
        echo "******deploy service $ser done! ******"
	done
}




main(){
	#use andpay as default group and username 		

    if [ -z $group ]; then
		group=andpay
	fi
	
	if [ -z $username ]; then
		username=andpay
	fi
	
	if [ -z $workspace ]; then
		workspace=$WORKSPACE_PATH
	fi

	if [ "$method" == "deploy" ]; then 
        local remotehost=`cat $HOST_CFG_PROP_FILE | grep "$service" | cut -d'=' -f2`
		deploy $service $remotehost $username
	elif [ "$method" == "ssl" -o "$method" == "s" ]; then
        if [ ! -z $service ]; then
            host=`cat $HOST_CFG_PROP_FILE | grep "$service" | cut -d'=' -f2`
        fi
        sslcfg $username $host
	elif [ "$method" == "init" -o "$method" == "i" ]; then
        init $username $host
    elif [ "$method" == "deployall" ]; then 
        if [ -z $service ]; then
            deployall ${allJars[@]}
        else
            deployall ${service[@]}
        fi     
    elif [ "$method" == "log" -o "$method" == "l" ]; then
        showlog $username $service
    fi
	
}

usage(){
	#echo "-u username, -d project directory, -i hostname/ip , -g group name ,-m method to call "
    echo -e "\nThe following options are available:\n"
    echo -e "\t-m method name to call. options[ssl|log|init|deploy|deployall]"
    echo -e "\t-s name of the service to be deployed"
    echo -e "\t-h host/ip to connect"
    echo -e "\t-u username used to connect, default is andpay"
    echo -e "\t-g group to create"
    echo -e "\t-d projects workspace, default is ~\n"
   
    
	echo -e "\tUsage: $0 -m s|ssl -h host/ip"
    echo -e "\tUsage: $0 -m l|log -s serviceName"
    echo -e "\tUsage: $0 -m i|init -u username -h host"
    echo -e "\tUsage: $0 -m deploy -s serviceName"
    echo -e "\tUsage: $0 -m deployall"
    echo -e "\tUsage: $0 -m deployall -s \"service1 service2\""
	echo -e "\tUsage: $0 -m deploy -d /path/to/workspace/diretory -s serviceName\n"
}


while getopts ":s:m:u:h:g:d" opt; do
  case $opt in
   	 u)
	 	username=$OPTARG
	    ;;
	 h)
	  	host=$OPTARG
	  	;;
	 g)
	  	group=$OPTARG
	  	;;
	 d)	  	
	  	workspace=$OPTARG
	  	;;
	 m)
	  	method=$OPTARG
	  	;;
	 s)
	  	service=$OPTARG
	  	;; 	
     \?)	
      	usage
      	exit 1
      	;;
     :)
        usage
      	exit 1
      	;;
  esac
done

main


