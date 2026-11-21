# Headphones (NeoForge 26.3)
Craft: iron ingots (top row) + note blocks (bottom corners). Wear in the head slot.
While worn: hostile/neutral/ambient/weather sounds are muted.

Music (client side, only while worn; taking them off stops it):
  /hp play <link>     e.g. /hp play https://www.youtube.com/watch?v=...
  /hp stop
  /hp volume <0-100>

Needs yt-dlp and ffmpeg installed and on PATH.

Build: copy gradlew, gradlew.bat and gradle/ from the official NeoForge MDK (JDK 25), then ./gradlew build
Jar appears in build/libs/.
