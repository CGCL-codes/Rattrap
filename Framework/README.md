# Framework

This consists of 6 benchmark workloads and the offloading framework we used in our experiments.

The workloads are as follows, and details can be found in our paper.

- AntiVirus
- CalculusTools
- CuckooChessAPK
- FaceDetect
- Linpack
- OCR

### DirectoryService ###
This is actually the *Dispatcher* in *Rattrap*. To run it, you need to export the project as a runnable jar and run

	$ java -jar DirService.jar

You need a MySQL DB to maintain the states of *Cloud Android Containers* by import the sql file we provide.

### LxcOff-Library and Lxcoff-Server ###
LxcOff-Library is the offloading library acquired when you want to modify your application to adopt *Rattrap*. The implementation details can be found in the workload source codes.

Lxcoff-Server is the server part running in *Cloud Android Container*. You should not pay more attention to it, since the rootfs has installed it. 


