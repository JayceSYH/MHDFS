# MHDFS
A simple distributed file system implemented by Java.





### **Guide**

#####  Step1

Run the eureka server by start main function of EurekaServerApplication class in the eurekaserver folder.

The default host is localhost and port is 8913, you can explore the index page by [http://localhost:8913](eureka index page)



##### Step2

Run the name server by start main function of NameserverApplication class in the nameserver folder.

The default host is localhost and port is 8913, you can explore the index page by [http://localhost:8910](file manager index page)

You can not create file unless you have node server registered, but you can create and explore folder without any registered node server.

 

##### step3

Run the node server by start main function of NodeserverApplication class in the nameserver(nameserver1, nameserver2 are the same) folder.

The default host is localhost and port for nameserver is 8915(8916, 8917 for nameserver1 and nameserver2).

By adding node server to the system, you can now upload files and download files exist in node servers.