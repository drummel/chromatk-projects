#!/usr/bin/env python3
"""
Naga Network Device Checker
Simple script to check if ESP32s and Raspberry Pi are alive on the local network
"""

import subprocess
import platform
import sys
from datetime import datetime

# List of device hostnames to check
DEVICES = [
    "naga-head.local",
    "naga-h1.local", 
    "naga-h2.local",
    "naga-h3.local",
    "naga-tail.local",
    "naga-pi.local"
]

def ping_host(hostname):
    """
    Ping a hostname and return True if reachable, False otherwise
    """
    # Determine ping command based on operating system
    system = platform.system().lower()
    if system == "windows":
        ping_cmd = ["ping", "-n", "1", "-w", "3000", hostname]
    else:  # Linux, macOS, etc.
        ping_cmd = ["ping", "-c", "1", "-W", "3", hostname]
    
    try:
        # Run ping command
        result = subprocess.run(
            ping_cmd, 
            capture_output=True, 
            text=True, 
            timeout=5
        )
        return result.returncode == 0
    except subprocess.TimeoutExpired:
        return False
    except Exception:
        return False

def check_all_devices():
    """
    Check all devices (ESP32s and Raspberry Pi) and print results
    """
    print("=" * 50)
    print("Naga Network Device Status Check")
    print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 50)
    
    alive_count = 0
    total_count = len(DEVICES)
    
    for hostname in DEVICES:
        print(f"Checking {hostname}...", end=" ")
        
        if ping_host(hostname):
            print("✅ ALIVE")
            alive_count += 1
        else:
            print("❌ NOT REACHABLE")
    
    print("=" * 50)
    print(f"Summary: {alive_count}/{total_count} devices are online")
    
    if alive_count == total_count:
        print("🎉 All Naga devices are alive and well!")
    elif alive_count == 0:
        print("⚠️  No Naga devices are reachable")
    else:
        print(f"⚠️  {total_count - alive_count} device(s) are not responding")
    
    return alive_count, total_count

def main():
    """
    Main function
    """
    try:
        alive, total = check_all_devices()
        # Exit with non-zero code if any devices are down
        sys.exit(0 if alive == total else 1)
    except KeyboardInterrupt:
        print("\n\nCheck interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\nError occurred: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()