# PyRunner Python Helper Module
# This module provides utility functions for the Android app

import sys
import os

def get_python_info():
    """Get Python environment information"""
    info = {
        'version': sys.version,
        'platform': sys.platform,
        'executable': sys.executable,
        'path': sys.path
    }
    return info

def clear_screen():
    """Clear the console screen"""
    os.system('cls' if os.name == 'nt' else 'clear')

def print_separator(char='-', length=40):
    """Print a separator line"""
    print(char * length)

def format_output(title, content):
    """Format output with a title"""
    print_separator('=')
    print(f' {title}')
    print_separator('-')
    print(content)
    print_separator('=')
