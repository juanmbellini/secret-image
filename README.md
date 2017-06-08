# Secret Image
 Cryptography and Security implementation project
 
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
 
 ## Authors
 * Juan Marcos Bellini
 * Diego de Rochebouët
 * Leandro Llorca
 * Jose Vitali