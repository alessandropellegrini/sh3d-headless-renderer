# Sweet Home 3D Headless Renderer

This project allows to render SH3D videos on a headless machine. The rendering uses the YafaRay renderer, with the highest quality.
It is not very well engineered, due to the clumsy quality of some dependencies.

To compile the project, you need Java 11 and can run `compile.sh`.
To launch the rendering on a headless machine, you can use `run.sh` with the following supported arguments:

```
  -f, --fps=<fps>         Frames per second
  -h, --height=<height>   Redering height
  -i, --input=<input>     SH3D File
  -o, --output=<output>   output file to create
  -s, --speed=<speed>     Camera speed (m/s)
  -w, --width=<width>     Redering width
```
