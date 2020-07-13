# stream-from-db

This spring boot program is an example how to extract data into csv files and zip them together on the fly.

In order to execute it you need sqlserver database.
Create there a demo database.  create user sa with password sa.

Start he application and you can call to :
http://localhost:8080/stream-demo/employees/3/cars

A zip file will be downloaded contains several csv files.
With no wait time.
