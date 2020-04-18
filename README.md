# COS420-Patient-Application

To run Patient App 2: 
   1) Must have postgres DB on google cloud (default name ghs)
   2) DB name, instance name, un/pw must be specified in the pom file 
   3) Patients table not necessary, but if you have one it should be created with
        CREATE TABLE IF NOT EXISTS patients ( id SERIAL PRIMARY KEY, 
					      firstName VARCHAR(255), 
					      lastName VARCHAR(255), 
					      gender VARCHAR(255), 
					      address VARCHAR(255), 
					      birthDate DATE);
