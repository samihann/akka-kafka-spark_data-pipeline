#!/bin/bash

cd /root/LogFileGenerator &&
sbt clean compile run &&
aws s3 cp /root/LogFileGenerator/log/* s3://course-project-datastreaming/ &&
rm -rf /root/LogFileGenerator/log/*