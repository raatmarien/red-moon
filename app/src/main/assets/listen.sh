#!/bin/bash

pipe=$1

trap "rm -f $pipe" EXIT

while true
do
    if read line <$pipe; then
        if [[ "$line" == 'quit' ]]; then
            break
        fi
        service call SurfaceFlinger $line
    fi
done
