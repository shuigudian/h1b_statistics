#!/bin/bash
#
# Use this shell script to compile (if necessary) your code and then execute it. Below is an example of what might be found in this file if your program was written in Python
#

cd src
javac h1b_counting.java
java  h1b_couting ../input/h1b_input.csv ../output/top_10_occupations.txt ../output/top_10_states.txt

