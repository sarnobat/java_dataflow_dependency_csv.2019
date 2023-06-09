#!/bin/sh

#----------------------------------------------------------------------------
# DESCRIPTION		
# DATE				[:VIM_EVAL:]strftime('%Y-%m-%d')[:END_EVAL:]
# AUTHOR			ss401533@gmail.com                                           
#----------------------------------------------------------------------------

#set -e
set -o errexit

test $# -gt 0 && echo "args given" || echo "no args"
# TODO: string comparison check (both ways)

cat <<EOF | batcat --plain --paging=never --language sh --theme TwoDark
mvn assembly:single && find $PWD -maxdepth 50 -type f -iname "**class" | java -classpath /Volumes/git/github/java_dataflow_dependency_csv/java_csv_data_dependency/target/java_csv_data_dependency-1.0-SNAPSHOT-jar-with-dependencies.jar  com.rohidekar.Main 2>/dev/null | tee out.csv | tee dependencies.csv

now use d3_graph.sh
EOF
