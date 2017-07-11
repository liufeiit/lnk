#!/usr/bin/env python
# -*- coding: UTF-8 -*-

"""
app-tool.py <start|stop|restart|check|dump|deploy|rollback|showlog|version> app_name ins_num

Version 1.0.2
History：
1.0.1   添加JENKINS_STATUS，用于jenkins判断部署状态
1.0.2   添加线程dump、rc环境清空tcinf_quartz
1.0.3   修复jetty日志切割前，找不到启动日志的问题
"""

import os
import re
import shutil
import signal
import socket
import subprocess
import sys
import time
import zipfile

import psutil
import tailer
import termcolor

sys.path.append('/opt/scripts/tools')
import webapps

PKG_DIR = '/opt/install/'  # 应用包存放路径
LOG_TRACE_TIME = 20  # 日志跟踪超时时间
EXCEPTION_KW = ['Exception','Caused','exception']
JENKINS_STATUS = 0


def usage():
    """
    打印脚本使用方法
    """
    usage_app = termcolor.colored('app-tool <start|stop|restart|check|deploy|rollback|showlog|version> app_name ins_num', 'green')
    usage_static = termcolor.colored('app-tool <deploy|rollback> webapps', 'green')

    print 'Usage:\n\t%s\n\t%s' % (usage_app, usage_static)


def highlight(str, type):
    """
    高亮脚本执行结果
    """
    if type == 'warn':
        print termcolor.colored(str, 'red', attrs=['bold'])
    elif type == 'prompt':
        print termcolor.colored(str, 'blue')
    elif type == 'ok':
        print termcolor.colored(str, 'green')
    else:
        print termcolor.colored(str, 'yellow')


def get_env():
    """
    获取环境:dev, integration, rc, prod
    """
    try:
        env = os.environ["DeployEnv"]
    except KeyError:
        highlight('Get env failed', 'warn')
        highlight('Choose a env: dev, integration, rc, prod', 'prompt')
        env = raw_input('> ')
        
    if env not in ('dev', 'integration', 'rc', 'prod'):
        highlight('Only the following env are acceptable: dev, integration, rc, prod', 'warn')
        sys.exit(1)

    return env


def get_Xmx():
    """
    根据环境获取Xmx大小
    """
    env = get_env()
    highlight('Current env: %s' % env, 'ok')

    if env in ('prod'):
        Xmx = '1024M'
    elif env in ('dev', 'integration', 'rc'):
        Xmx = '512M'

    return Xmx


def get_var(app_name, ins_num):
    """
    获取应用各项参数:应用路径、日志路径、备份路径、回滚路径、应用前缀、应用类型
          app_base_dir
          |    app_prefix
          |    |       app_dir
          |    |       |                raw_dir
          |    |       |                |
    /opt/app2/web/biz-fnc-web2/webapps/ROOT/
    """
    if int(ins_num) == 1:
        log_base_dir = '/log/java/'
        app_base_dir = '/opt/app/'
        backup_base_dir = '/opt/backup/'
        rollback_base_dir = '/opt/rollback/'
    else:
        log_base_dir = '/log/java%s/' % ins_num
        app_base_dir = '/opt/app%s/' % ins_num
        backup_base_dir = '/opt/backup%s/' % ins_num
        rollback_base_dir = '/opt/rollback%s/' % ins_num

    if app_name.endswith('-srv'):
        app_prefix = app_name.split('-')[0]
        app_type = 'jar'
        if int(ins_num) == 1:
            app_dir = os.path.join(app_base_dir, app_prefix, app_name)
        else:
            app_dir = os.path.join(app_base_dir, app_prefix, app_name+ins_num)
        raw_dir = app_dir
    else:
        app_prefix = 'web'
        app_dir = os.path.join(app_base_dir, app_prefix, app_name+ins_num)
        if not os.path.exists(app_dir):
            highlight('Instance %s does not exist, create it first' % app_dir, 'warn')
            sys.exit(1)
        raw_dir = os.path.join(app_dir, 'webapps', 'ROOT')
        if os.path.exists(os.path.join(app_dir, 'bin', 'catalina.sh')):
            app_type = 'tomcat'
        elif os.path.exists(os.path.join(app_dir, 'bin', 'jetty.sh')):
            app_type = 'jetty'
        else:
            app_type = 'Unknown'

    return app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir

def get_jacoco(app_name, ins_num):
    jacoco_base_dir = '/opt/jacoco/'
    jacoco_exec_dir = '%sexec/' % jacoco_base_dir
    if os.path.exists(os.path.join(jacoco_exec_dir, app_name)):
        jacoco_app_dir = os.path.join(jacoco_exec_dir, app_name)
    else:
        os.mkdir(os.path.join(jacoco_exec_dir, app_name))
        jacoco_app_dir = os.path.join(jacoco_exec_dir, app_name)
    
    return jacoco_base_dir, jacoco_exec_dir, jacoco_app_dir	

def get_pid(app_name, ins_num):
    """
    获取应用的java进程PID
    """
    app_pid = []
    for proc in psutil.process_iter():
        if '-Dapp.name='+app_name in proc.cmdline() and '-Dins.num='+ins_num in proc.cmdline():
            app_pid.append(proc)

    return app_pid


def get_app_list(ins_num):
    """
    获取已经部署的应用列表
    """
    if int(ins_num) == 1:
        app_base_dir = '/opt/app/'
    else:
        app_base_dir = '/opt/app%s/' % ins_num

    app_list = []
    for i in os.listdir(app_base_dir):
        for j in os.listdir(os.path.join(app_base_dir, i)):
            app_list.append(j)
    app_list = [re.sub(r'[\d]+$', "", x) for x in sorted(app_list, reverse=False)]

    return app_list


def check_app(app_name, ins_num):
    """
    检查应用状态
    """
    pid = [i.pid for i in get_pid(app_name, ins_num)]
    pid_quantity = len(pid)
    if pid_quantity == 1:
        app_status = 'OK'
        highlight('%-25s%-10s%-15s%-15s' % (app_name, ins_num, app_status, pid), 'ok')
    elif pid_quantity == 0:
        app_status = 'Down'
        highlight('%-25s%-10s%-15s%-15s' % (app_name, ins_num, app_status, pid), 'warn')
    else:
        app_status = 'Unknown'
        highlight('%-25s%-10s%-15s%-15s' % (app_name, ins_num, app_status, pid), 'warn')

    return app_status


def thread_dump(app_name, ins_num):
    """
    应用线程dump
    """
    timestamp = time.strftime("%Y%m%d_%H%M%S",time.localtime(time.time()))
    dump_dir = os.path.join('/log', 'dump', app_name)
    dump_file = os.path.join(dump_dir, '%s_%s_%s_thread_dump.log' % (app_name, ins_num, timestamp))

    if not os.path.exists(dump_dir):
        os.mkdir(dump_dir)
    
    app_pid = get_pid(app_name, ins_num)
    if len(app_pid) == 0:
        highlight('%s already stopped' % app_name, 'warn')
        return
    else:
        pid = app_pid[0].pid

    with open(dump_file, 'w+') as f:
        p=subprocess.Popen(['/usr/java/default/bin/jstack', str(pid)], stdout=f)
        p.wait()
        f.flush()
        
    highlight('Thread dump file: %s' % dump_file, 'ok')


def track_log(app_name, ins_num):
    """
    跟踪启动日志
    """
    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)
    global JENKINS_STATUS
    exception_counter = 0
    if app_type == 'jar':
        startup_log = os.path.join(app_dir, 'bin', 'nohup.out')
        startup_kw = 'started'
    elif app_type == 'tomcat':
        startup_log = os.path.join(app_dir, 'logs', 'catalina.out')
        startup_kw = 'INFO: Server startup in'
    elif app_type == 'jetty':
        log_dir = os.path.join(app_dir, 'logs')
        log_files = [file for file in os.listdir(log_dir)]
        log_files.sort(key=lambda file: os.stat(os.path.join(log_dir,file)).st_mtime)
        startup_log = os.path.join(log_dir, log_files[-1])
        startup_kw = 'INFO:oejs.Server:main: Started'

    if os.path.exists(startup_log):
        highlight('Tracking startup log: %s' % startup_log, 'prompt')
        try:
            for line in tailer.follow(open(startup_log)):
                print line
                for kw in EXCEPTION_KW:
                    if kw in line:
                        exception_counter += 1
                if startup_kw.lower() in line.lower():
                    if exception_counter == 0:
                        highlight('%s startup successfully' % app_name, 'ok')
                    else:
                        JENKINS_STATUS = 1
                        highlight('%s startup with error, check %s please' % (app_name, startup_log), 'warn')
                    break
        except KeyboardInterrupt:
            highlight('Force quit tracking startup log', 'warn')
    else:
        JENKINS_STATUS = 1
        highlight('Log %s does not exist' % startup_log, 'warn')


def start_app(app_name, ins_num):
    """
    启动应用
    """
    app_pid = get_pid(app_name, ins_num)
    if len(app_pid) != 0:
        highlight('%s already started, stop it first' % app_name, 'warn')
        return

    highlight('Starting %s' % app_name, 'prompt')

    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)
    jacoco_base_dir, jacoco_exec_dir, jacoco_app_dir = get_jacoco(app_name, ins_num)
	
    global JENKINS_STATUS
    Xmx = get_Xmx()
    hostname = socket.gethostname()

    JAVA_OPTS = "-Xmx%s -XX:+HeapDumpOnOutOfMemoryError -Dapp.name=%s -Dins.num=%s -Dhost.name=%s -DINF_ALARM_LOG_DIR=%s/alarm/%s \
                 -Dtask.period=1 -javaagent:%sjacocoagent.jar=output=file,destfile=%s/%s_%s.exec" % (Xmx, app_name, ins_num, hostname, log_base_dir, app_name, jacoco_base_dir, jacoco_app_dir, app_name, ins_num)


    os.environ['logDir'] = log_base_dir
    os.environ['JAVA_OPTS'] = JAVA_OPTS

    if app_type == 'jar':
        if os.path.exists(os.path.join(app_dir, 'bin', app_name)):
            os.chdir(os.path.join(app_dir, 'bin'))
            psutil.Popen(["nohup", "/bin/bash", app_name])
        else:
            JENKINS_STATUS = 1
            highlight('%s does not exist, startup failed' % os.path.join(app_dir, 'bin', app_name), 'warn')
            return
    elif app_type == 'tomcat':
        os.chdir(os.path.join(app_dir, 'bin'))
        psutil.Popen(["/bin/bash", 'startup.sh'])
    elif app_type == 'jetty':
        os.chdir(os.path.join(app_dir, 'bin'))
        subprocess.call(["/bin/bash", 'jetty.sh', 'start'], stdout=subprocess.PIPE)  # 使用psutil.Popen启动jetty会卡住
    else:
        highlight('Type of %s is %s' % (app_name, app_type), 'warn')

    time.sleep(0.1)

    def signal_handler(signum, frame):
        raise Exception("Timed out!")
    
    signal.signal(signal.SIGALRM, signal_handler)
    signal.alarm(LOG_TRACE_TIME)
    try:
        track_log(app_name, ins_num)
    except Exception, msg:
        JENKINS_STATUS = 1
        highlight('Tracking startup log timeout(%ss)' % LOG_TRACE_TIME, 'warn')
    
    truncate_quartz_db()


def stop_app(app_name, ins_num):
    """
    停止应用
    """
    app_pid = get_pid(app_name, ins_num)
    if len(app_pid) == 0:
        highlight('%s already stopped' % app_name, 'warn')
        return

    highlight('Stopping %s' % app_name, 'prompt')

    for p in app_pid:
        highlight('Trying terminate %s' % p.pid, 'prompt')
        p.terminate()  # 尝试优雅停止
    gone, alive = psutil.wait_procs(app_pid, timeout=3)
    for p in alive:
        highlight('Terminate %s timeout, force kill it now' % p.pid, 'warn')
        p.kill()  # 优雅停止超时,强行kill


def restart_app(app_name, ins_num):
    """
    重启应用
    """
    stop_app(app_name, ins_num)
    time.sleep(1)
    start_app(app_name, ins_num)


def show_log(app_name, ins_num):
    """
    查看应用日志
    """
    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)
    log_file = os.path.join(log_base_dir, app_name, 'log', app_name+'.log')

    if os.path.exists(log_file):
        highlight('Tailing log %s' % log_file, 'prompt')
        for line in tailer.follow(open(log_file)):
            print line
    else:
        highlight('Log %s does not exist' % log_file, 'warn')


def backup_app(app_name, ins_num, deploy_type):
    """
    备份应用
    """
    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)
    timestamp = time.strftime("%Y%m%d_%H%M%S",time.localtime(time.time()))
    timestamp_date = timestamp.split('_')[0]
    timestamp_time = timestamp.split('_')[1]

    if deploy_type == 'deploy':
        backup_dir = os.path.join(backup_base_dir, timestamp_date, app_name+'_'+timestamp_time)  # 正常部署与回滚时备份的位置不同
    elif deploy_type == 'rollback':
        backup_dir = os.path.join(rollback_base_dir, timestamp_date, app_name+'_'+timestamp_time)

    if os.path.exists(raw_dir):
        shutil.copytree(raw_dir, backup_dir)
        highlight('Backup %s to %s successfully' % (app_dir, backup_dir), 'ok')
    else:
        highlight('Backup failed, cause %s does not exist' % raw_dir, 'warn')


def update_app_config(app_name, ins_num):
    """
    更新.app_config
    """
    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)
    env = get_env()

    highlight('Updating .app_config with env: %s' % env, 'prompt')

    if app_type == 'jar':
        os.chdir(raw_dir)
    else:
        os.chdir(os.path.join(raw_dir, 'WEB-INF', 'classes'))
    psutil.Popen(['config-tool', env])


def replace(file, src_str, dst_str):
    """
    替换文件中的字符串(不产生临时文件)
    """
    fopen=open(file,'r')
    w_str=""
    for line in fopen:
        if re.search(src_str, line):
            line=re.sub(src_str, dst_str, line)
            w_str+=line
        else:
            w_str+=line
    wopen=open(file,'w')
    wopen.write(w_str)
    fopen.close()
    wopen.close()


def replace_logback_xml(app_name, ins_num):
    """
    替换logback.xml中写死的'/log/java/'
    """
    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)
    logback_file = os.path.join(raw_dir, 'WEB-INF', 'classes', 'logback.xml')

    if not os.path.exists(logback_file):
        highlight('Replace logback.xml failed, cause %s does not exist' % logback_file, 'warn')
        return
    
    highlight('Replacing logback.xml: %s' % logback_file, 'prompt')

    replace(logback_file, '/log/java/', log_base_dir)


def replace_tianwang_env(app_name, ins_num):
    """
    替换天网日志系统中写死的'env=test'
    """
    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)

    if app_type == 'jar':
        lib_dir = os.path.join(raw_dir, 'lib')
    else:
        lib_dir = os.path.join(raw_dir, 'WEB-INF', 'classes')

    for jar in os.listdir(lib_dir):
        if jar.startswith(app_name):
            os.chdir(lib_dir)
            archive = zipfile.ZipFile(jar, 'a')
            for file in archive.namelist():
                if file.startswith('application.properties'):
                    archive.extract(file, lib_dir)
                    highlight('Replacing tianwang log env', 'prompt')
                    replace('application.properties', 'env=test', 'env=product')
                    archive.write('application.properties')  # 替换后要重新写入jar


def deploy_app(app_name, ins_num):
    """
    部署应用
    应用部署步骤:确认新包存在→备份→停止应用→删除原应用→解压新包→更新app_config→替换logback&天网日志配置→启动应用
    """
    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)
    global JENKINS_STATUS
    env = get_env()

    pkg_list = []
    for i in os.listdir(PKG_DIR):
        if re.match(app_name+'-\d', i):
            pkg_list.append(i)
    pkg_list = sorted(pkg_list, reverse=True)  # 获取应用新包,并排序

    seq = 1
    for pkg in pkg_list:
        print "%d): %s" % (seq, pkg)
        seq += 1

    if len(pkg_list) == 0:
        JENKINS_STATUS = 1
        highlight('No such package, deploy canceled', 'warn')
        return
    elif len(pkg_list) == 1:
        app_pkg = os.path.join(PKG_DIR, pkg_list[0])
    else:
        highlight('select NUMBER from above', 'prompt')
        try:
            select = int(raw_input('> '))
            app_pkg = os.path.join(PKG_DIR, pkg_list[select-1])
        except:
            highlight('No such package, deploy canceled', 'warn')
            return

    highlight('Selected package is %s' % app_pkg, 'prompt')
    highlight('Deploying %s' % app_name, 'prompt')

    backup_app(app_name, ins_num, 'deploy')  # 备份
    stop_app(app_name, ins_num)  # 停止应用
    if os.path.exists(raw_dir):
        shutil.rmtree(raw_dir)  # 删除原应用
        highlight('Delete %s successfully' % raw_dir, 'ok')
    else:
        highlight('Delete failed, cause %s does not exist' % raw_dir, 'warn')

    pkg_ref = zipfile.ZipFile(app_pkg, 'r')
    pkg_ref.extractall(raw_dir)  # 解压新包
    pkg_ref.close()
    highlight('Unzip %s to %s successfully' % (app_pkg, raw_dir), 'ok')

    update_app_config(app_name, ins_num)  # 更新.app_config
    time.sleep(1)
    if env in ('rc', 'prod'):
        replace_tianwang_env(app_name, ins_num)  # 替换天网日志环境配置
    if not app_type == 'jar':
        replace_logback_xml(app_name, ins_num)  # 替换logback.xml
    start_app(app_name, ins_num)  # 启动应用


def rollback_app(app_name, ins_num):
    """
    回滚应用
    应用回滚步骤:确认备份存在→备份回滚前应用→停止应用→删除回滚前应用→还原备份→更新app_config→启动应用
    """
    app_base_dir, log_base_dir, backup_base_dir, rollback_base_dir, app_prefix, app_type, app_dir, raw_dir = get_var(app_name, ins_num)

    backup_list = []
    for i in os.listdir(backup_base_dir):
        for j in os.listdir(os.path.join(backup_base_dir, i)):
            if j.startswith(app_name):
                backup_list.append(os.path.join(backup_base_dir, i, j))
    backup_list = sorted(backup_list, reverse=True)  # 获取应用备份,并排序

    seq = 1
    for bak in backup_list:
        print "%d): %s" % (seq, bak)
        seq += 1

    if len(backup_list) == 0:
            highlight('No such backup, rollback canceled', 'warn')
            return
    else:
        highlight('select NUMBER from above', 'prompt')
        try:
            select = int(raw_input('> '))
            backup_for_rollback = os.path.join(backup_list[select-1])
        except:
            highlight('No such backup, rollback canceled', 'warn')
            return

    highlight('Selected backup is %s' % backup_for_rollback, 'prompt')
    highlight('Rollbacking %s' % app_name, 'prompt')

    backup_app(app_name, ins_num, 'rollback')   # 备份(至rollback目录)
    stop_app(app_name, ins_num)  # 停止应用
    if os.path.exists(raw_dir):
        shutil.rmtree(raw_dir)  # 删除原应用
        highlight('Delete %s successfully' % raw_dir, 'ok')
    else:
        highlight('Delete failed, cause %s does not exist' % raw_dir, 'warn')

    shutil.copytree(backup_for_rollback, raw_dir)  # 还原备份
    highlight('Rollback %s to %s successfully' % (backup_for_rollback, raw_dir), 'ok')

    update_app_config(app_name, ins_num)  # 更新.app_config
    time.sleep(1)
    start_app(app_name, ins_num)  # 启动应用


def truncate_quartz_db():
    """
    rc环境清空独立的tcinf_quartz、tcinf_ttl数据库
    """
    if get_env() == 'rc':
        highlight('Truncating tcinf_quartz、tcinf_ttl', 'prompt')
        os.chdir(os.path.join('/opt','scripts'))
        psutil.Popen(['/bin/bash','truncate.sh'])
    

def run(operation, app_name, ins_num):
    """
    单应用操作入口
    """
    if operation == 'start':
        start_app(app_name, ins_num)
    elif operation == 'stop':
        stop_app(app_name, ins_num)
    elif operation == 'restart':
        restart_app(app_name, ins_num)
    elif operation in ('check', 'status'):
        check_app(app_name, ins_num)
    elif operation == 'dump':
        thread_dump(app_name, ins_num)
    elif operation == 'showlog':
        show_log(app_name, ins_num)
    elif operation == 'deploy':
        deploy_app(app_name, ins_num)
    elif operation == 'rollback':
        rollback_app(app_name, ins_num)
    else:
        usage()
        sys.exit(1)


def main():
    """
    主入口(包括多应用操作)
    """
    if os.geteuid() != 700:    # 仅允许op(uid统一为700)运行
        highlight("This script must run as 'op'", 'warn')
        sys.exit(1)

    if len(sys.argv) == 4:
        operation = sys.argv[1]
        app_name = sys.argv[2]
        ins_num = sys.argv[3]
    else:
        usage()
        sys.exit(1)

    if app_name == '-':
        if operation in ('check', 'status'):
            highlight('%-25s%-10s%-15s%-15s' % ('APP_NAME', 'INS_NUM', 'APP_STATUS', 'PID'), 'prompt')
            app_list = get_app_list(ins_num)
            for app in app_list:
                run(operation, app, ins_num)
        else:
            highlight("Confirm with [y/yes]", 'prompt')
            yes = set(['yes','y'])
            choice = raw_input('> ').lower()
            if choice in yes:
                app_list = get_app_list(ins_num)
                for app in app_list:
                    highlight("%s %s" % (operation, app), 'ok')
                    run(operation, app, ins_num)
                    time.sleep(1)
            else:
                highlight("Operation canceled", 'warn')
                sys.exit(1)
    elif app_name == 'webapps':
        if operation == 'deploy':
            webapps.run('update')
        elif operation == 'rollback':
            webapps.run('rollback')
        else:
            highlight("Operation %s not support for webapps" % operation, 'warn')
    else:
        if operation in ('check', 'status'):
            highlight('%-25s%-10s%-15s%-15s' % ('APP_NAME', 'INS_NUM', 'APP_STATUS', 'PID'), 'prompt')
        run(operation, app_name, ins_num)


if __name__ == '__main__':
    main()
    if JENKINS_STATUS == 0:
        sys.exit(0)
    else:
        sys.exit(1)

