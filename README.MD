## Authorizer App
#### How to run:
You should have atleast Java *1.8* version installed on your laptop
I have tried to run it only on Windows bash console, but believe it would work correct on Unit systems as well

**Commands to run inside project directory:**
> ```sbt "clean; test; assembly"``` - run modular test and build jar file locally from the project directory

> ```${project_directory}/target/scala-2.13/nubank-assignment-0.1.jar``` - directory of a built jar file

> ```cat ${oparations_file_path} - | java -jar ${application_file_path}``` - run this command to start publishing data to StdIn of your application

> ```java -jar ${application_file_path} < ${oparations_file_path}``` - or you can try running this one

Example you can run from the project directory after building .jar:
```
cat src/main/resources/use-cases.txt - | java -jar target/scala-2.13/nubank-assignment-0.1.jar
```

Input example:
```
{"account": {"activeCard": true, "availableLimit": 100}}
{"transaction": {"merchant": "Burger King", "amount": 20, "time": "2019-02-13T11:00:00.000Z"}}
{"transaction": {"merchant": "Habbib's", "amount": 20, "time": "2019-02-13T11:00:01.000Z"}}
{"transaction": {"merchant": "McDonald's", "amount": 20, "time": "2019-02-13T11:01:01.000Z"}}
{"transaction": {"merchant": "Subway", "amount": 20, "time": "2019-02-13T11:01:31.000Z"}}
```

Output example:
```
{"account":{"activeCard":true,"availableLimit":100},"violations":[]}
{"account":{"activeCard":true,"availableLimit":80},"violations":[]}
{"account":{"activeCard":true,"availableLimit":60},"violations":[]}
{"account":{"activeCard":true,"availableLimit":40},"violations":[]}
{"account":{"activeCard":true,"availableLimit":40},"violations":["high-frequency-small-interval"]}
```

The format of data in the assignment is different and I think that it is a mistake
> ```{"account": {"activeCard": true, "availableLimit": 100}}``` - Correct field names pattern (Supported only this field Names) 

> ```{"account": {"active-card": true, "available-limit": 225}}``` - Incorrect field names pattern
