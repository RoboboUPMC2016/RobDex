# RobDex
Server for .java -> .dex compilation


Usage: java robDex.Server [options]


Le latest (currently 2.3.0) retrolambda jar file can be found here:
https://oss.sonatype.org/content/groups/public/net/orfjackal/retrolambda/retrolambda/


Options:


-p, --port   value
   Port on which the server will listen.
   Default: 5668


-j, --jar   path
   Full path (including its name) to the jar file containing thé dependencies.
   Default: the program will look for a jar file which name does not begin with “retrolambda” in the current directory.


-x, --executable   path
   Full path (including its name) to the dx file.
   Default: the program will assume dx is a valid command.


-d, --directory   path
   Directory in which the program must store temporary files.
   Default: a file named tmp will be made in de current directory.


-r, --retrolambda path
   Full path (including its name) to the retrolambda jar file.
   Default: the program will look for a jar file which name begins with “retrolambda” in the current directory.

