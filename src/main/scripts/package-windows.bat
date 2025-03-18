jpackage ^
  --name BFVyze ^
  --input target ^
  --main-jar bfvyze-1.0.0.jar ^
  --main-class fr.antschw.bfv.Main ^
  --type exe ^
  --icon src/main/resources/icon.ico ^
  --java-options "--enable-preview" ^
  --win-dir-chooser ^
  --win-shortcut
