Isolation tests for OSGi EventAdmin and Subsystem

How to build:

1. download and build Apache Aries https://svn.apache.org/repos/asf/aries/
2. mvn install

How to run:

1. mvn -P run 
2. install and start a subsystem 2

      sub:install mvn:subsystem_ea_tests/subsystem_2/1.0/esa
      
      sub:start 1
      
3. install and start a subsystem 1

      sub:install mvn:subsystem_ea_tests/subsystem_1/1.0/esa
      
      sub:start 2    
