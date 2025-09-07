# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [ReactiveX](https://reactivex.io)
* [Project Reactor](https://projectreactor.io)

### Create Maven SpringBoot Reactor Project
```
	$ curl https://start.spring.io/starter.zip?name=spring-boot-webflux&groupId=com.kevinpina&artifactId=spring-boot-webflux&version=0.0.1-SNAPSHOT&description=Webflux+demo+project+for+Spring+Boot&packageName=com.kevinpina&type=maven-project&packaging=jar&javaVersion=17&language=java&bootVersion=3.5.3&dependencies=lombok&dependencies=devtools&dependencies=data-mongodb-reactive&dependencies=thymeleaf&dependencies=webflux
```

### Install MongoDB
* [MongoDB Community Edition](https://www.mongodb.com/try/download/community)
* [Robo 3T](https://robomongo.org)

```
	# Install Tools
		$ sudo apt install gnupg curl
		$ curl -fsSL https://pgp.mongodb.com/server-7.0.asc |sudo gpg  --dearmor -o /etc/apt/trusted.gpg.d/mongodb-server-7.0.gpg
	
	# Add Repo MongoDB
		$ echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
		$ sudo apt update
	
	# Install MongoDB
		$ sudo apt install -y mongodb-org
	
	# Start MongoDB
		$ sudo systemctl start mongod
	
	# Verify MongoDB
		$ sudo systemctl status mongod
	
	# Start MongoDB on system startup (optional)
		$ sudo systemctl enable mongod
	
	# Run MongoDB
		$ mongosh
		$ mongosh --host 127.0.0.1 --port 27017
	
	# Enable MongoDB Remotely
		$ sudo vi /etc/mongod.conf
			# Current
				net:
				  port: 27017
				  bindIp: 127.0.0.1
		  	# Change for
		  		net:
				  port: 27017
				  bindIp: 0.0.0.0

	# Enabled MongoDB User/Password
		## Create User admin:
			$ mongosh
				> use admin
				> db.createUser({
				  user: "admin",
					pwd: "MyPassword",
					roles: [ { role: "userAdminAnyDatabase", db: "admin" }, "readWriteAnyDatabase" ]
				})

		## Create UserApp "kevin"
			$ mongosh
				> use enterprise
				> db.createUser({
				  user: "kevin",
				  pwd: "MyPassword",
				  roles: [ { role: "readWrite", db: "enterprise" } ]
				})

		## Show Users
			> use admin
			> db.system.users.find();

		## Activar autenticacion 
			$ vi /etc/mongod.conf
				security:
					authorization: enabled

		## Reinicia MongoDB
			$ sudo systemctl restart mongod

		## Login as admin
			$ mongosh -u "admin" -p "MyPassword" --authenticationDatabase "admin"

		## Loin as kevin
			$ mongosh -u kevin -p --authenticationDatabase enterprise

		## Add Access to Other Database  
			# Log as admin
			$ mongosh -u "admin" -p "MyPassword" --authenticationDatabase "admin"

				> use newDataBase
				
				> db.grantRolesToUser("myUser1", [
					{ role: "readWrite", db: "newDataBase" }
				])

				> db.grantRolesToUser("myUser2", [
					{ role: "readWrite", db: "newDataBase" },
					{ role: "dbAdmin", db: "otherDataBase" }
				])

				> db.getUsers();
				
				> db.getUser("myUser1");    # Log as myUser1 (or as admin then > use newDataBase, then execute this command will show the permissions.

				-- Roles: 
					* readWrite: Allows reading and writing to the database.
					* read: Allows only reading data from the database.
					* dbAdmin: Allows managing the database (e.g., creating indexes).
					* userAdmin: Allows managing users and roles within a database.

				-- Global Roles: 
					* readWriteAnyDatabase
					* dbAdminAnyDatabase
					* userAdminAnyDatabase
```

### Examples MongoDB
```
	# Show Current Database
		$ db
		
    # Show Database
		$ show databases
		$ show dbs
		
	# Change/Create db
		$ use mydb	# This switches to mydb, and automatically creates it when you insert data. 
						# Now insert something to actually create the DB.

	# Show Collections "like Tables"
		$ show collections
	
	# Insert/Create Collection (if no exists will create users)
		$ db.users.insertMany([{"id":1, "name": "kevin", "surname": "pina"}, {"id":2, "name": "javier", "surname": "calatrava"}]);
		
    # Rename Collection
        $ db.getCollection("users").renameCollection("product"); 
        
    # Drop Collection
        $ db.getCollection("product").drop();

	# Select
		$ db.users.find({})
		$ db.users.find({"name": "kevin"})
		$ db.users.find().limit(1)
		$ db.getSiblingDB("mydb").getCollection("users")
              .find({})
              .limit(21)
```

### Run
```
    $ curl http://localhost:8080/list
    
    $ curl http://localhost:8080/list-datadriver1
    
    $ curl http://localhost:8080/list-datadriver2
    
    $ curl http://localhost:8080/list-full

    $ curl http://localhost:8080/list-chunked
    
    $ curl http://localhost:8080/api/products
    $ curl http://localhost:8080/api/products/v1/id/{id}
    $ curl http://localhost:8080/api/products/v2/id/{id}
    
    $ curl http://localhost:8080/list
    $ curl http://localhost:8080/form
    $ curl --request POST http://localhost:8080/form
```
