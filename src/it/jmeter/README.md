## Test it!

CLI mode always!
```
 jmeter -Jthreads=<Number of threads> -Jrampup=<rampup in seconds> -Jduration=<duration in seconds> -Jiterations=<Number of iterations, -1 means infinite> -n -t "<jmx test file location>" -e -l "<output log file location>" -o "<report output folder>"
```
Remember to erase the output file and report folder among executions!