#!/bin/bash

defEnv=stage0
pgkDir=$HOME/install
appDir=$HOME/app
backupDir=$HOME/backup
rollbackDir=$HOME/rollback
autoKill=1
maxLogTraceTime=20

# jars with correct deploy order
allJars=(bgw-common-srv bgw-payment-srv bgw-payment-wxpay-srv)

# wars with correct deploy order, entity=(projectName|webContainerName|webAppName)
allWars=('bgw-payment-api-web|bgw-payment-api-web-jetty|ROOT' 'bgw-payment-notify-web|bgw-payment-notify-web-jetty|ROOT' 'bgw-payment-web|bgw-payment-web-jetty|ROOT') 

deployJar() {
	curDir=`pwd`
	env=$1
	fp=$2 #file path
	
	echo -e "\n>start deploying $fp"
	fn=${fp##*/} #fileName
	pn=`echo $fn | sed 's/\(.*\)-[0-9]\{1,\}\..*/\1/'` #projectName
	pc=`echo $pn | sed 's/\([^-]*\)-.*/\1/'` #projectCategory
	pd=$appDir/$pc/$pn #projectDir
	
	#kill running process
	test "$autoKill" == "1" &&
	pids=`ps -ef | grep $pn | grep -v bash | grep -v grep | awk '{printf $2 " "}'` &&
	test "$pids" != "" && echo ">kill -9 $pids" && kill -9 $pids 
	
	#backup
	tbd=$backupDir/`date +%Y%m%d` #init today's backup dir
	test ! -d $tbd && mkdir -p $tbd
	
	bpd=$tbd/$pn.jar_dir_`date '+%H%M%S'` #backup project dir
	test -d $pd && echo ">backup $pd to $bpd" && mv $pd $bpd 

	#init and start
	mkdir -p $pd && echo ">copy $fp to $pd" && cp $fp $pd 
	cd $pd && echo ">extract files and config env" && jar xf $fn && rm -rf $fn && config-tool $env
	cd bin && chmod u+x $pc* && echo ">starting $pn ..." && nohup ./$pc* < /dev/null 1>nohup.out 2>&1 &
	
	#trace log
	sleep 1s
	traceStartupLog $pd/bin/nohup.out
	
	cd $curDir
}

rollbackJar() {
	curDir=`pwd`
	env=$1
	rp=$2 #rollback path
	
	echo ">start rollbacking $2"
	pn=${rp%.*} #projectName
	pn=${pn##*/}
	pc=${pn%%-*} #projectCategory
	pd=$appDir/$pc/$pn #projectDir
	
	#kill running process
	test "$autoKill" == "1" &&
	pids=`ps -ef | grep $pn | grep -v bash | grep -v grep | awk '{printf $2 " "}'` &&
	test "$pids" != "" && echo ">kill -9 $pids" && kill -9 $pids
	
	#backup
	trd=$rollbackDir/`date +%Y%m%d` #init today's rollback dir
	test ! -d $trd && mkdir -p $trd
	
	rpd=$trd/$pn.jar_dir_`date '+%H%M%S'` #rollback project dir
	test -d $pd && echo ">backup $pd to $rpd" && mv $pd $rpd

	#init and start
	mkdir -p $pd && echo ">copy $rp to $pd" && cp -r $rp/* $pd
	cd $pd && echo ">config env" && config-tool $env
	cd bin && chmod u+x $pc* && echo ">starting $pn ..." && nohup ./$pc* < /dev/null 1>nohup.out 2>&1 &
	
	#trace log
	sleep 1s
	traceStartupLog $pd/bin/nohup.out
	
	cd $curDir
}

deployWar() {
	curDir=`pwd`
	env=$1
	fp=$2 #file path
	
	echo -e "\n>start deploying $fp"
	fn=${fp##*/} #fileName
	pn=`echo $fn | sed 's/\(.*\)-[0-9]\{1,\}\..*/\1/'` #projectName
	pc=`echo $pn | sed 's/\([^-]*\)-.*/\1/'` #projectCategory
	
	# find war info
	wi=`findWarInfo $pn | grep '^[^|]\{1,\}|[^|]\{1,\}|[^|]\{1,\}$'`
	if [ "$wi" == "" ]
	then
		echo "Cannot find war info for deployment, projectName=$pn"
		exit 1
	fi
	
	wcn=`echo $wi | cut -d '|' -f 2` #webContainerName
	wan=`echo $wi | cut -d '|' -f 3` #webAppName
	
	wcd=$appDir/$pc/$wcn #webContainerDir
	wad=$wcd/webapps/$wan #webAppDir
	
	#kill running process
	test "$autoKill" == "1" &&
	pids=`ps -ef | grep $pn | grep -v bash | grep -v grep | awk '{printf $2 " "}'` &&
	test "$pids" != "" && echo ">kill -9 $pids" && kill -9 $pids 
	
	#backup
	tbd=$backupDir/`date +%Y%m%d` #init today's backup dir
	test ! -d $tbd && mkdir -p $tbd
	
	bpd=$tbd/$pn.war_dir_`date '+%H%M%S'` #backup project dir
	test -d $wad && echo ">backup $wad to $bpd" && mv $wad $bpd 

	#init and start
	mkdir -p $wad && echo ">copy $fp to $wad" && cp $fp $wad 
	cd $wad && echo ">extract files and config env" && jar xf $fn && rm -rf $fn && cd WEB-INF && config-tool $env
	cd $wcd && chmod u+x $pc* && echo ">starting $pn ..." && nohup ./$pc* < /dev/null 1>nohup.out 2>&1 &
	
	#trace log
	sleep 1s
	traceStartupLog $wcd/nohup.out
	
	cd $curDir
}

rollbackWar() {
	curDir=`pwd`
	env=$1
	rp=$2 #rollback path
	
	echo ">start rollbacking $2"
	pn=${rp%.*} #projectName
	pn=${pn##*/}
	pc=${pn%%-*} #projectCategory

	# find war info
	wi=`findWarInfo $pn | grep '^[^|]\{1,\}|[^|]\{1,\}|[^|]\{1,\}$'`
	if [ "$wi" == "" ]
	then
		echo "Cannot find war info for deployment, projectName=$pn"
		exit 1
	fi
	
	wcn=`echo $wi | cut -d '|' -f 2` #webContainerName
	wan=`echo $wi | cut -d '|' -f 3` #webAppName
	
	wcd=$appDir/$pc/$wcn #webContainerDir
	wad=$wcd/webapps/$wan #webAppDir
	
	#kill running process
	test "$autoKill" == "1" &&
	pids=`ps -ef | grep $pn | grep -v bash | grep -v grep | awk '{printf $2 " "}'` &&
	test "$pids" != "" && echo ">kill -9 $pids" && kill -9 $pids
	
	#backup
	trd=$rollbackDir/`date +%Y%m%d` #init today's rollback dir
	test ! -d $trd && mkdir -p $trd
	
	rpd=$trd/$pn.war_dir_`date '+%H%M%S'` #rollback project dir
	test -d $wad && echo ">backup $wad to $rpd" && mv $wad $rpd

	#init and start
	mkdir -p $wad && echo ">copy $rp to $wad" && cp -r $rp/* $wad
	cd $wad && echo ">config env" && cd WEB-INF && config-tool $env
	cd $wcd && chmod u+x $pc* && echo ">starting $pn ..." && nohup ./$pc* < /dev/null 1>nohup.out 2>&1 &

	#trace log
	sleep 1s
	traceStartupLog $wcd/nohup.out
	
	cd $curDir
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

traceStartupLog() {
	if test -e $1
	then
	  echo ">start tailing nohup file, path=$1"
	  tail -f $1 & tpid=$!
	  {
		for i in $(seq 1 $maxLogTraceTime)
		do
			lastLog=`tail -n 1 $1 | grep -i started`
			if [ "${lastLog}" != "" ]
			then
				kill -9 $tpid
				echo -e ">project start successful, elapsed time: ${i}s\n"
				break
			elif [ $i -eq $maxLogTraceTime ]
			then
				kill -9 $tpid
				echo -e ">project hasn't been started in past ${maxLogTraceTime}s, please check nohup file manually...\n"
				break
			fi
			sleep 1s
		done
	  }
	else
	  echo -e ">cannot find the nohup file, path=$1 \n"
	fi
}

choose() {
	if [ $# == 1 ]
	then
		return 0
	fi
	
	options=($@)
	optionSize=${#options[@]}
	for ((i=1; i<=$optionSize; i++))
	do
		echo "$i: ${options[$i-1]}"
	done
	
	while true
	do
    	read -p ">please select [1-$optionSize]: " selNo
	
	    if [ $selNo -ge 1 ] 2>/dev/null && [ $selNo -le $optionSize ] 2>/dev/null
	    then
	    	echo ">the selected is ${options[$selNo-1]}"
	    	read -p ">please confirm [y/n]: " cfm
	    	if [[ $cfm == [yY] ]]
	    	then
				return $[selNo-1]
	        fi
	    fi
	done
}

case "${1:-''}" in
	'list')
		pattern=${2:-''}
		echo "=== List install files ==="
		case "$pattern" in
			'')
				pattern='.*\(install\.jar\|\.war\)'
				;;
			'jar')
				pattern='.*install\.jar'
				;;
			'war')
				pattern='.*\.war'
				;;
			*)
				echo 'Usage: 1) $0 list [jar|war]'
				exit 1
				;;
		esac
		
		find $pgkDir -regex "$pattern"
		;;
		
	'jar')
		env=${3:-$defEnv}
		prefix=${2:-''}
		case "$prefix" in
			'')
				echo "Usage: $0 jar prefix|- [env:$defEnv]"
				;;
			'-')
				echo "=== Deploy all jar ==="
				for pn in ${allJars[@]}
				do
					bash $0 jar $pn
				done
				;;
			*)
				echo "=== Deploy jar $prefix* ==="
				files=(`find $pgkDir -name "$prefix*install.jar"|head -255`)
				if [ ${#files[@]} == 0 ]
				then
					echo "file not found"
					exit 1
				fi
				choose ${files[@]}
				deployJar $env ${files[$?]}
				;;
			esac
		;;
		
	'raj')
		env=${3:-$defEnv}
		prefix=${2:-''}
		case "$prefix" in
			'')
				echo "Usage: $0 raj prefix [env:$defEnv]"
				;;
			*)
				echo "=== Rollback jar $prefix* ==="
				files=(`find $backupDir -maxdepth 2 -type d ! -empty -name "$prefix*"|grep "^.*/[0-9]\{8\}/.*\.jar_dir_[0-9]\{6\}$"|sort -r|head -255`)
				if [ ${#files[@]} == 0 ]
				then
					echo "file not found"
					exit 1
				fi
				
				choose ${files[@]}
				rollbackJar $env ${files[$?]}
				;;
			esac
		;;
		
	'war')
		env=${3:-$defEnv}
		prefix=${2:-''}
		case "$prefix" in
			'')
				echo "Usage: $0 war prefix|- [env:$defEnv]"
				;;
			'-')
				echo "=== Deploy all war ==="
				for wi in ${allWars[@]}
				do
					bash $0 war `echo $wi | cut -d '|' -f 1`
				done
				;;
			*)
				echo "=== Deploy war $prefix* ==="
				files=(`find $pgkDir -name "$prefix*.war"|head -255`)
				if [ ${#files[@]} == 0 ]
				then
					echo "file not found"
					exit 1
				fi
				
				choose ${files[@]}
				deployWar $env ${files[$?]}
				;;
			esac
		;;
		
	'raw')
		env=${3:-$defEnv}
		prefix=${2:-''}
		case "$prefix" in
			'')
				echo "Usage: $0 raw prefix [env:$defEnv]"
				;;
			*)
				echo "=== Rollback war $prefix* ==="
				files=(`find $backupDir -maxdepth 2 -type d ! -empty -name "$prefix*"|grep "^.*/[0-9]\{8\}/.*\.war_dir_[0-9]\{6\}$"|sort -r|head -255`)
				if [ ${#files[@]} == 0 ]
				then
					echo "file not found"
					exit 1
				fi
				
				choose ${files[@]}
				rollbackWar $env ${files[$?]}
				;;
			esac
		;;
		
	*)
		echo "Usage: 1) $0 list [jar|war]"
		echo "       2) $0 jar|war prefix|- [env:$defEnv]"
		echo "       3) $0 raj|raw prefix [env:$defEnv]"
		exit 1
		;;
esac
