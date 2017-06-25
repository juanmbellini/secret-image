# Secret Image
Cryptography and Security implementation project.

## Getting Started

These instructions will install the system in your local machine.

### Prerequisites

1. Install Maven, if you haven't yet
    #### Mac OS X
    ```
    brew install maven
    ```

    #### Ubuntu
    ```
    sudo apt-get install maven
    ```

    #### Other OSes
    Check https://maven.apache.org/install.html.

2. Clone the repository or download source code:
```
git clone https://github.com/juanmbellini/secret-image.git
```

### Compiling and Installing

1. Change working directory to project root (i.e where ```pom.xml``` is located):
```
cd <project-root>
```

2. Let maven resolve dependencies:
```
mvn dependency:resolve
```

3. Create jar file
```
mvn clean package [-D dir=<destination-directory>]
```
Note: If no destination was indicated, jar file will be under ``` <project-root>/target ```

## Usage
The application can be executed running ```java -jar <path-to-jar>```.

### Options
In order to execute using different options, parameters must be passed to the program. The following sections will describe these parameters

#### Displaying usage message
You can display the usage message by setting the ```-h``` or the ```--help``` parameters.
Example of usage:
```
java -jar <path-to-jar> -h
```

#### Distribution mode
In order to run in distribution mode, you must include the  ```-d``` parameter.
In distribution mode, the system will create shadow images in order to hide the secret image in them.
At least one execution mode must be included.
Distribution and Recovery are mutually exclusive modes. Do not include both together.
Example of usage:
```
java -jar <path-to-jar> -d -secret image.bmp -k 8
```

#### Recovery mode
In order to run in recovery mode, you must include the  ```-r``` parameter.
In recovery mode, the system will recreate the secret image from the shadow images.
At least one execution mode must be included.
Distribution and Recovery are mutually exclusive modes. Do not include both together.
Example of usage:
```
java -jar <path-to-jar> -r -secret image.bmp -k 8
```

#### Secret Image
In order to indicate the secret image path, you must include the ```-secret``` parameter.
If running in distribution mode, this will be the image to be hidden in the shadow images.
Otherwise, if running in recovery mode, this will be the output (i.e the recovered image).
This is a required parameter.
Example of usage:
```
java -jar <path-to-jar> -r -secret ~/Pictures/image.bmp -k 8
```

#### Minimum amount of shadows
In order to indicate the secret image path, you must include the ```-k``` parameter.
This must be a positive integer number.
This is a required parameter.
Example of usage:
```
java -jar <path-to-jar> -d -secret image.bmp -k 6
```

#### Amount of shadows
In order to indicate the amount of shadows to be created, you must include the ```-n``` parameter.
This must be a positive integer number, greater of equal than the minimum amount of shadows.
This is an optional parameter. If not included, the system will count how much image files there are in the shadows directory, and use that number as the amount of shadows.
Example of usage:
```
java -jar <path-to-jar> -d -secret image.bmp -k 6 -n 8
```

#### Shadows directory
In order to indicate the shadows directory, you must include the ```-dir```parameter.
If running in distribution mode, this directory must contain the images that will be used as shadows (i.e where the secret image will be hidden).
Otherwise, if running in recovery mode, the directory must contain the shadow images.
This must be a valid path to a directory.
This is an optional parameter. If not included, the system will use the current working directory as the shadows directory.
Example of usage:
```
java -jar <path-to-jar> -d -secret image.bmp -k 6 -dir ~/Pictures/shadows
```



## Authors
* Juan Marcos Bellini
* Diego de Rochebouët
* Leandro Llorca
* Jose Vitali

## Acknowledgments
Math utils are based on https://github.com/nayuki/Nayuki-web-published-code project