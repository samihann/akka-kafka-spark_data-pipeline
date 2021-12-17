#!/bin/bash

echo "Execution-Started" >> /root/assembly_spark/emailLogs
# cd to folder where report is generated
zip -r /root/assembly_spark/reports/report.zip /root/assembly_spark/reports/*


( echo "Subject: ALERT | Messages with ERROR noticed" ; echo ; echo "Hello, The recent batch of messages has multiple Error messages. Please Check. Thank You.  "; uuencode /root/assembly_spark/reports/report.zip report.zip ) | sendmail -vf svn2998@gmail.com svn2998@gmail.com
mkdir -p /root/assembly_spark/reportBackup && mv /root/assembly_spark/reports/* /root/assembly_spark/reportBackup/