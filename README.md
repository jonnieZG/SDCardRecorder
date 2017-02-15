# SDCardRecorder
A simple utility for copying WAV and MP3 sound files to an SD Card used in DFPlayer Mini module, and naming/organizing them for optimal performance.

The files will be copied respecting their natural (sort-by-name) order. It also creates an *.H* file with definitions of sound file indexes and their original names in the comment. That file will be written to SD as *9999.H*, just for your reference only - it does not affect the sound card functionalities in any way

The utility also formats the SD prior to copying, and names the files as *0000.EEE* (0000 being order number and EEE the original
 file extension.
