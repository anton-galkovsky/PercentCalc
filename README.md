# PercentCalc

This project's target is to detect drunkenness of the mobile phone owner analyzing his gait using built-in accelerometer. 

Current database is situated in folders named "new_\*". Its analysis evidences that drunk gait has differences from sober gait and can be detected for some people with high accuracy.

To perform analyze on your own tests you should:
* Create a file that will contain rows "time:accel_module:accel_x:accel_y:accel_z" (see example in the database)
* Add it to project folder and its name to Main.java with number 30 for sober measurement and 80 for drunk)
* Comment out file names in Main.java that should no be analyzed
* Set "draw_\*" constants in Constants.java
* Launch function main in Main.java

Depending on "draw_\*" constants there will become several ghaphs of the following types:
* Input data from accelerometer
* Heat graph that shows what local period has input wave in the different times
* Calculated amplitudes and durations of the every step with they local deviations
* The distribution of step amplitudes and durations deviatiations in the different parts of measurements
