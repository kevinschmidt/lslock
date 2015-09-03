# lslock for Mesosphere

### Task

Write a program, lslock, that accepts a directory argument and prints the PIDs and paths of all files locked beneath it. Write a test program, lslock-test, that launches a few background processes to lock files in the directory /tmp/lslock-test and verifies that your lslock does indeed find all the locks. If more than one process holds a lock on the same file — for example, a parent and one of its children — it is okay to list the lock multiple times.

You may use the language of your choice. Please include instructions for building and running both lslock and lslock-test.

### Running instructions

`./lslock.sh [directory]` to list files

`./lslock-test.sh` to run tests

Requirements: JVM 7 or up, SBT, Linux server (tested on Ubuntu 14.04 LTS)
