
#find /Users/ssarnobat/webservices/cmp/authentication-services/target/classes/ -iname "*class" \
#	|  /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/bin/java -Dfile.encoding=UTF-8 -classpath /Users/ssarnobat/github/java_data_dependency_csv/java_csv_data_dependency/target/classes:/Users/ssarnobat/.m2/repository/com/google/guava/guava/19.0/guava-19.0.jar:/Users/ssarnobat/.m2/repository/org/apache/bcel/bcel/6.0/bcel-6.0.jar:/Users/ssarnobat/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:/Users/ssarnobat/.m2/repository/commons-io/commons-io/2.4/commons-io-2.4.jar com.rohidekar.Main
	
# 2018	
find /Users/ssarnobat/webservices/cmp/authentication-services/target/classes/ -iname "*class"      |  /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/bin/java -Dfile.encoding=UTF-8 -classpath /Users/ssarnobat/github/java_data_dependency_csv/java_csv_data_dependency/target/classes:/Users/ssarnobat/.m2/repository/com/google/guava/guava/19.0/guava-19.0.jar:/Users/ssarnobat/.m2/repository/org/apache/bcel/bcel/6.0/bcel-6.0.jar:/Users/ssarnobat/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:/Users/ssarnobat/.m2/repository/commons-io/commons-io/2.4/commons-io-2.4.jar com.rohidekar.Main 2>/dev/null | perl -pe 's{\s*...depends on( variable)?....\s*}{","}g' | perl -pe 's{(^.*$)}{"$1"}g' | sort | tee variable_dependencies.csv	

# 2019
cd ~/github/java_dataflow_dependency_csv/java_csv_data_dependency/ && find $PWD -type f -iname "Simple.class" | JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_211.jdk/Contents/Home/ mvn --quiet compile exec:java -Dexec.mainClass=com.rohidekar.Main 2>/dev/null | perl -pe 's{\s*...depends on( variable)?....\s*}{","}g' | perl -pe 's{(^.*$)}{"$1"}g' | sort | tee /tmp/variable_dependencies.csv

# 2021
/Library/Java/JavaVirtualMachines/jdk1.8.0_211.jdk/Contents/Home

# Now use d3_helloworld_csv.git/singlefile_automated/ for visualization
cd /Users/srsarnob/github/d3_csv/singlefile_automated && cat /tmp/variable_dependencies.csv  | sh csv2d3.sh  | tee /tmp/index.html && popd