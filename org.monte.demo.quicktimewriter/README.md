# Monte Media - QuickTimeWriter Demo

This demo showcases the QuickTimeWriter class.

## How to run the demo

Execute the following commands in the terminal:

```console
cd ..
mvn clean package
java -p org.monte.demo.quicktimewriter/target/classes:org.monte.media/target/classes -m org.monte.demo.quicktimewriter/org.monte.demo.quicktimewriter.Main
```

The demo produces the following output files:

| File                        | Codec                      | Quality  | Pixel Format  |
|-----------------------------|----------------------------|----------|---------------|
| quicktimedemo-jpg-q0.75.mov | JPEG                       | 75%      | 24-bit color  |
| quicktimedemo-png.mov       | PNG                        | lossless | 24-bit color  |
| quicktimedemo-raw8.mov      | QuickTime Uncompressed RGB | lossless | 8-bit indexed |
| quicktimedemo-raw24.mov     | QuickTime Uncompressed RGB | lossless | 24-bit color  |
| quicktimedemo-rle8.mov      | QuickTime Animation        | lossless | 8-bit indexed |
| quicktimedemo-rle16.mov     | QuickTime Animation        | lossless | 16-bit color  |
| quicktimedemo-rle24.mov     | QuickTime Animation        | lossless | 24-bit color  |
| quicktimedemo-tscc8.mov     | TechSmith Screen-Capture   | lossless | 8-bit indexed |
| quicktimedemo-tscc8gray.mov | TechSmith Screen-Capture   | lossless | 8-bit gray    |
| quicktimedemo-tscc16.mov    | TechSmith Screen-Capture   | lossless | 16-bit color  |
| quicktimedemo-tscc24.mov    | TechSmith Screen-Capture   | lossless | 24-bit color  |

## How to view the output files

| File                        | Can be opened with                                     | Known issues          |
|-----------------------------|--------------------------------------------------------|-----------------------|
| quicktimedemo-jpg-q0.75.mov | Windows Media Player, QuickTime Player, VLC, Handbrake | none                  |
| quicktimedemo-png.mov       | VLC, Handbrake                                         | none                  |
| quicktimedemo-raw8.mov      | -                                                      | none                  |
| quicktimedemo-raw24.mov     | Handbrake                                              | none                  |
| quicktimedemo-rle8.mov      | VLC                                                    | none                  |
| quicktimedemo-rle16.mov     | VLC, Handbrake                                         | none                  |
| quicktimedemo-rle24.mov     | VLC, Handbrake                                         | none                  |
| quicktimedemo-tscc8.mov     | VLC                                                    | color table is broken |
| quicktimedemo-tscc8gray.mov | VLC, Handbrake                                         | color table is broken |
| quicktimedemo-tscc16.mov    | VLC, Handbrake                                         | none                  |
| quicktimedemo-tscc24.mov    | VLC, Handbrake                                         | none                  |


