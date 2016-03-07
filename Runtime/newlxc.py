#!/usr/bin/python

import os
import sys
import getopt

name = "default"
cgroup_cpuset = None
cgroup_cpushare = "1024"
cgroup_mem = "100"
ip = None

def parse_cmdline_options(arglist):
    global name, cgroup_cpuset, cgroup_cpushare, cgroup_mem, ip
    try:
        optlist, args = getopt.getopt(arglist, "n:", ["ip=", "cgroup-cpuset=", "cgroup-cpushare=", "cgroup-mem="])
    except getopt.error as e:
        command_line_error("Bad command line option: %s" % (str(e),))

    for opt, arg in optlist:
        if opt == "--cgroup-cpuset":
            cgroup_cpuset = arg
        elif opt == "--cgroup-cpushare":
            cgroup_cpushare = arg
        elif opt == "--cgroup-mem":
            cgroup_mem = arg
        elif opt == "-n":
            name = arg
        elif opt == "--ip":
            ip = arg
        else:
            command_line_error("Unkown option %s" % opt)

    return args


def command_line_error(message):
    """Indicate a command line error and exit"""
    sys.stderr.write("Error: %s\n" % (message,))
    sys.stderr.write("See the rdiffdir manual page for instructions\n")
    sys.exit(1)

def prepare_container():
    os.system("mkdir /var/lib/lxc/"+name)
    os.system("tar -zxf ./rootfs.tar.gz -C /var/lib/lxc/"+name)

    """Create config file"""
    configfile = open("/var/lib/lxc/"+name+"/config", "w")
    configfile.write("lxc.network.type = veth\n")
    configfile.write("lxc.network.link = lxcbr0\n")
    configfile.write("lxc.network.flags = up\n")
    #configfile.write("lxc.network.ipv4 = "+ ip +"/24 10.0.3.255\n")
    configfile.write("lxc.rootfs = /var/lib/lxc/"+ name +"/rootfs\n")
    configfile.write("lxc.aa_allow_incomplete = 1\n")
    configfile.write("lxc.init_cmd = /init\n")
    configfile.write("lxc.autodev = 0\n")
    configfile.write("lxc.pts = 1024\n")
    configfile.write("lxc.tty = 4\n")
    configfile.write("lxc.hook.pre-start = /var/lib/lxc/"+ name +"/pre-start.sh\n")
    configfile.write("lxc.hook.post-stop = /var/lib/lxc/"+ name +"/post-stop.sh\n")
    if cgroup_cpuset != None:
        configfile.write("lxc.cgroup.cpuset.cpus = "+ cgroup_cpuset +"\n")
    configfile.write("lxc.cgroup.cpu.shares = "+ cgroup_cpushare +"\n")
    configfile.write("lxc.cgroup.memory.limit_in_bytes = "+ cgroup_mem +"M\n")
    configfile.close()

    post_stop_file = open("/var/lib/lxc/"+name+"/post-stop.sh", "w")
    post_stop_file.write("#!/bin/sh\n")
    post_stop_file.write("rm -rf /var/lib/lxc/"+name+"/rootfs/dev/__properties__\n")
    post_stop_file.write("umount /var/lib/lxc/"+name+"/rootfs/system")
    post_stop_file.close()

    pre_start_file = open("/var/lib/lxc/"+name+"/pre-start.sh", "w")
    pre_start_file.write("#!/bin/sh\n")
    pre_start_file.write("mount --bind /root/system /var/lib/lxc/"+name+"/rootfs/system")
    pre_start_file.close()

def main():
    args = parse_cmdline_options(sys.argv[1:])
    prepare_container()

if __name__ == "__main__":
    main()
