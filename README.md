Classroom Manager
=================

This application is built using the [Ninja Framework](http://www.ninjaframework.org/).

Setup for Development
---------------------

Software required:
- Java (1.7 or better)
- Maven (3.11 or better)
- mysql database running locally (you will need to change config if you run elsewhere)

Run the following in mysql to create the user and databases for dev and testing

```
create user 'appd'@'localhost' identified by '4v^Xa7P1el';
create database classman_dev;
create database classman_test;
grant all on classman_dev to 'appd'@'localhost';
grant all on classman_test to 'appd'@'localhost';
```
Save your aws credentials (create a key in aws) in ~/.aws/credentials

```
[default]
aws_access_key_id = <your key id>
aws_secret_access_key = <your key>
```

To import project into eclipse, just import as a maven project.

Building and running locally
----------------------------

From the application directory:

```
mvn clean install
mvn ninja:run
```

The application will be available at http://localhost:8080/

The default login is 'admin'/'admin'.

