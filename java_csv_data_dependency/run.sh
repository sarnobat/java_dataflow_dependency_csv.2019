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
mvn -q assembly:single && find $PWD -maxdepth 50 -type f -iname ".class" | grep analyze | java -classpath /Volumes/git/github/java_dataflow_dependency_csv/java_csv_data_dependency/target/java_csv_data_dependency-1.0-SNAPSHOT-jar-with-dependencies.jar  com.rohidekar.Main 2>/dev/null | tee out.csv | tee dependencies.csv

now do:
	cat dependencies.csv | sort | uniq | tee /tmp/1.html | sh /tmp/filter_public.sh | sort | uniq | tee /tmp/1.html | sh /tmp/filter_public.sh | grep -v "var null" | sh /tmp/csv2d3.sh | tee /tmp/index2.html
EOF
