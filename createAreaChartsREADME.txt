Steps to make data ready for the createAreaXXXX.m function:
1. Matrix = deterPcts(getPcts(time, answerTypes))
2. SortedTime = sort(time);
3. createAreaTime(SortedTime, Matrix);



there are two helper functions for making the area chart.

getPcts.m takes two vectors (x axis, y axis)

in this case: time and question types
or	      distance and question types

and creates a 4 column matrix that totals the answer types over the course of the x-axis, and is row sorted based on the x-axis



deterPcts.m takes the matrix from getPcts.m and converts each row to four percent values that add to 1.


createAreaXXXX.m requires a sorted x-axis (sorted time or sorted distance) and the matrix from getPcts.m






