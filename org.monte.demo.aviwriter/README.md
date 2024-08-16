# Monte Media - AviWriter Demo

This demo showcases the AviWriter class.

## How to run the demo

Execute the following commands in the terminal:

```console
cd ..
mvn clean package
java -p org.monte.demo.aviwriter/target/classes:org.monte.media/target/classes -m org.monte.demo.aviwriter/org.monte.demo.aviwriter.Main
```

The demo produces the following output files:

| File                  | Codec                               | Quality  | Pixel Format  |
|-----------------------|-------------------------------------|----------|---------------|
| avidemo-jpg-q0.75.avi | JPEG                                | 75%      | 24-bit color  |
| avidemo-png.avi       | PNG                                 | lossless | 24-bit color  |
| avidemo-raw8.avi      | Microsoft Device Independent Bitmap | lossless | 8-bit indexed |
| avidemo-raw8gray.avi  | Microsoft Device Independent Bitmap | lossless | 8-bit gray    |
| avidemo-raw24.avi     | Microsoft Device Independent Bitmap | lossless | 24-bit color  |
| avidemo-rle8.avi      | Microsoft Run Length Encoding       | lossless | 8-bit indexed |
| avidemo-rle8gray.avi  | Microsoft Run Length Encoding       | lossless | 8-bit gray    |
| avidemo-tscc8.avi     | TechSmith Screen-Capture            | lossless | 8-bit indexed |
| avidemo-tscc8gray.avi | TechSmith Screen-Capture            | lossless | 8-bit gray    |
| avidemo-tscc16.avi    | TechSmith Screen-Capture            | lossless | 16-bit color  |
| avidemo-tscc24.avi    | TechSmith Screen-Capture            | lossless | 24-bit color  |

## How to view the output files

| File                  | Can be opened with                                     | Known issues          |
|-----------------------|--------------------------------------------------------|-----------------------|
| avidemo-jpg-q0.75.avi | Windows Media Player, QuickTime Player, VLC, Handbrake | none                  |
| avidemo-png.avi       | VLC, Handbrake                                         | none                  |
| avidemo-raw8.avi      | VLC, Handbrake                                         | index is broken       |
| avidemo-raw8gray.avi  | Handbrake                                              | none                  |
| "                     | VLC                                                    | color table is broken |
| avidemo-raw24.avi     | VLC, Handbrake                                         | none                  |
| avidemo-rle8.avi      | VLC, Handbrake                                         | index is broken       |
| avidemo-rle8gray.avi  | Handbrake                                              | none                  |
| "                     | VLC                                                    | color table is broken |
| avidemo-tscc8.avi     | VLC, Handbrake                                         | index is broken       |
| avidemo-tscc8gray.avi | Handbrake                                              | none                  |
| "                     | VLC                                                    | color table is broken |
| avidemo-tscc16.avi    | VLC, Handbrake                                         | none                  |
| avidemo-tscc24.avi    | VLC, Handbrake                                         | none                  |


