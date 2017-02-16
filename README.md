# SDCardRecorder
A simple utility for copying WAV and MP3 sound files to an SD Card used in  **`Arduino`** **`DFPlayer Mini`** module, and naming/organizing them for optimal performance.

It copies files originally organized in subfolders, respecting their natural (sort-by-name) order, flattening their structure for faster referencing by the DFPlayer, required for gapless play. Their reference numbers are created into an .H file with definitions of sound file indexes and their original names in the comment. That file will be written to SD as `9999.H`, just for your reference only - it does not affect the sound card functionalities in any way.

The utility also formats the SD prior to copying, and names the files as *`0000.EEE`* (*`0000`* being order number and *`EEE`* the original
 file extension. The original files in source directory remain unchanged.

This is an example of the generated header file:

```c++
#define SND_EFF_BEEP		1 /* 01-EFF/001-beep.mp3 */
#define SND_EFF_BEEP_FUZZY		2 /* 01-EFF/002-Beep Fuzzy.wav */
#define SND_EFF_SELECTED		3 /* 01-EFF/003-Selected.wav */
#define SND_EFF_BACK		4 /* 01-EFF/004-Back.wav */
#define SND_EFF_SHUTTER		5 /* 01-EFF/005-Shutter.wav */
#define SND_SETTINGS_BATTERY_LEVEL		6 /* 02-Settings/00 Battery Level.mp3 */
#define SND_SETTINGS_RELEASE_FOR_SETTINGS		7 /* 02-Settings/000-0 Release for Settings.mp3 */
#define SND_SETTINGS_SETTINGS		8 /* 02-Settings/000-1 Settings.mp3 */
```
