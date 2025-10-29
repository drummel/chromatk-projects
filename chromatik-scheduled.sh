#!/bin/bash

# Chromatik scheduler - runs from 30min before sunset to midnight
# Auto-restarts if Java process crashes during active hours

# Configuration
LATITUDE="37.7749"   # San Francisco latitude - adjust for your location
LONGITUDE="-122.4194" # San Francisco longitude - adjust for your location
CACHE_FILE="/tmp/chromatik_sunset_cache"

# Global variables
CURRENT_DATE=""
SUNSET_TIME=""
START_TIME=""

# Function to get today's sunset time
get_sunset_time() {
    local today=$(date +%Y-%m-%d)
    
    # Check if we have today's sunset cached
    if [ -f "$CACHE_FILE" ]; then
        local cached_date=$(head -n1 "$CACHE_FILE")
        if [ "$cached_date" = "$today" ]; then
            tail -n1 "$CACHE_FILE"
            return 0
        fi
    fi
    
    echo "$(date): Fetching sunset time for $today..."
    
    # Get sunset time from API
    local response=$(curl -s "https://api.sunrise-sunset.org/json?lat=$LATITUDE&lng=$LONGITUDE&formatted=0&date=$today")
    
    if [ $? -eq 0 ] && echo "$response" | grep -q '"status":"OK"'; then
        # Extract sunset time and convert to local time (HH:MM format only)
        local sunset_utc=$(echo "$response" | grep -o '"sunset":"[^"]*"' | cut -d'"' -f4)
        local sunset_local=$(date -d "$sunset_utc" +%H:%M 2>/dev/null)
        
        if [ $? -eq 0 ] && [ -n "$sunset_local" ] && [[ "$sunset_local" =~ ^[0-9]{2}:[0-9]{2}$ ]]; then
            # Cache the result
            echo "$today" > "$CACHE_FILE"
            echo "$sunset_local" >> "$CACHE_FILE"
            echo "$sunset_local"
            return 0
        fi
    fi
    
    echo "$(date): Failed to get sunset time, using fallback (20:00)"
    echo "20:00"  # Fallback to 8 PM
}

# Function to calculate start time (30 minutes before sunset)
get_start_time() {
    local sunset_time="$1"
    local sunset_hour=$(echo "$sunset_time" | cut -d: -f1)
    local sunset_minute=$(echo "$sunset_time" | cut -d: -f2)
    
    # Convert to minutes since midnight
    local sunset_minutes=$((sunset_hour * 60 + sunset_minute))
    local start_minutes=$((sunset_minutes - 30))
    
    # Handle case where start time is before midnight (negative)
    if [ $start_minutes -lt 0 ]; then
        start_minutes=$((start_minutes + 1440))  # Add 24 hours
    fi
    
    # Convert back to HH:MM
    local start_hour=$((start_minutes / 60))
    local start_min=$((start_minutes % 60))
    
    printf "%02d:%02d\n" $start_hour $start_min
}

# Function to update sunset times if date changed
update_sunset_if_needed() {
    local today=$(date +%Y-%m-%d)
    
    if [ "$CURRENT_DATE" != "$today" ]; then
        CURRENT_DATE="$today"
        SUNSET_TIME=$(get_sunset_time)
        START_TIME=$(get_start_time "$SUNSET_TIME")
        echo "$(date): Date changed to $today. Sunset: $SUNSET_TIME, Start time: $START_TIME"
    fi
}

# Function to check if current time is in the active window
is_active_time() {
    local start_time="$1"
    local current_time=$(date +%H:%M)
    local current_hour=$(date +%H)
    
    # Convert times to minutes since midnight for easier comparison
    local start_hour=$(echo "$start_time" | cut -d: -f1)
    local start_minute=$(echo "$start_time" | cut -d: -f2)
    local start_minutes=$((start_hour * 60 + start_minute))
    
    local current_minutes=$((($(date +%H) * 60) + $(date +%M)))
    
    # Active from start_time until 23:59 (before midnight)
    if [ $current_minutes -ge $start_minutes ] && [ $current_hour -lt 24 ] && [ $current_hour -ne 0 ]; then
        return 0  # Active
    else
        return 1  # Not active
    fi
}

# Initialize sunset times
update_sunset_if_needed

while true; do
    # Check if date changed and update sunset times
    update_sunset_if_needed
    
    current_hour=$(date +%H)
    
    if is_active_time "$START_TIME"; then
        echo "$(date): Starting Chromatik service (sunset-based schedule)"
        
        # Keep running during the active window
        while true; do
            current_hour=$(date +%H)
            
            # Exit if we've reached midnight
            if [ "$current_hour" -eq 0 ]; then
                echo "$(date): Active window ended (midnight)"
                break
            fi
            
            # Start the Java process directly
            cd /home/naga/Chromatik
            /home/naga/.sdkman/candidates/java/current/bin/java \
             -Dsun.net.inetaddr.ttl=0 \
             -Dsun.net.dns.spi.nameservice.provider.1=dns,sun \
             -Dsun.net.dns.spi.nameservice.provider.2=mdns,sun \
             -cp Chromatik-1.0.1-SNAPSHOT/chromatik-1.0.1-SNAPSHOT-linux-aarch64.jar heronarts.lx.studio.Chromatik \
             --accept-eula --require-license --headless Projects/naga-ggp.lxp &
            CHROMATIK_PID=$!
            echo "$(date): Chromatik process started (PID: $CHROMATIK_PID)"
            
            # Monitor the process
            while kill -0 $CHROMATIK_PID 2>/dev/null; do
                current_hour=$(date +%H)
                
                # Stop at midnight (00:00)
                if [ "$current_hour" -eq 0 ]; then
                    echo "$(date): Stopping Chromatik service (midnight)"
                    kill $CHROMATIK_PID
                    wait $CHROMATIK_PID 2>/dev/null
                    break 2  # Break out of both loops
                fi
                
                sleep 30  # Check every 30 seconds
            done
            
            # If we get here, the process died before midnight
            current_hour=$(date +%H)
            if [ "$current_hour" -eq 0 ]; then
                # We reached midnight, exit normally
                break
            else
                # Process crashed during active hours, restart it
                echo "$(date): Chromatik process died unexpectedly, restarting in 10 seconds..."
                sleep 10
                # Continue the while loop to restart
            fi
        done
        
        echo "$(date): Chromatik service stopped"
    fi
    
    # Sleep for a minute before checking again
    sleep 60
done