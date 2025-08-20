
# Pre-requisites
* Java 1.8/1.11/1.15
* Gradle 6

# How to run the code

We have provided scripts to execute the code. 

Use `run.sh` if you are Linux/Unix/macOS Operating systems and `run.bat` if you are on Windows.

Internally both the scripts run the following commands 

 * `gradle clean build -x test --no-daemon` - This will create a jar file `geektrust.jar` in the `build/libs` folder.
 * `java -jar build/libs/geektrust.jar sample_input/input1.txt` - This will execute the jar file passing in the sample input file as the command line argument

 Use the build.gradle file provided along with this project. Please change the main class entry under the `jar` task

 ```
 manifest {
        attributes 'Main-Class' : 'com.geektrust.backend.App' //Change this to the main class of your program which will be executed
    }
```
in the build.gradle if your main class has changed.

 # How to execute the unit tests

 `gradle clean test --no-daemon` will execute the unit test cases.

# Help

You can refer our help documents [here](https://help.geektrust.in)
You can read build instructions [here](https://github.com/geektrust/coding-problem-artefacts/tree/master/Java)



# Geektrust MyMoney Backend Challenge evaluation result :

![MyMoney_Score.jpg](images%2FMyMoney_Score.jpg )
![MyMoney_Badges.jpg](images%2FMyMoney_Badges.jpg)
![MyMoney_Readability.jpg](images%2FMyMoney_Readability.jpg)
![MyMoney_Maintainability.jpg](images%2FMyMoney_Maintainability.jpg)
![MyMoney_Build.jpg](images%2FMyMoney_Build.jpg)