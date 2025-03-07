#!/bin/bash

# Get list of containers and their status
containers=$(curl -s http://localhost:2375/containers/json?all=true)

# Loop through each container
echo "$containers" | jq -c '.[]' | while read container; do
    id=$(echo "$container" | jq -r '.Id')
    name=$(echo "$container" | jq -r '.Names[0]')
    state=$(echo "$container" | jq -r '.State')

    # If container is not running, restart it
    if [ "$state" != "running" ]; then
        echo "Restarting container: $name ($id)"
        curl -s --request POST http://localhost:2375/containers/$id/restart
    fi
done
