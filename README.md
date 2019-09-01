DevilUtil
=========

#### A Java game library

Not quite a game engine, but not far off either.

**Overview of each package:**

* **devil.al** - Wraps OpenAL, and allows convenient sound loading, and playback with filters/effects like reverb.
* **devil.game** - Wraps GLFW, and handles window creation, frame synchronization, mouse and keyboard input, and gamepad input.
* **devil.geo2d** - Basic 2D geometry helper classes.
* **devil.geo3d** - 3D geometry classes, including "robust" and "fast" ray tracing and collision detection for meshes and ellipsoids.
* **devil.gl** - Blazing-fast and easy to use OpenGL wrapper, fully forward compatible for OpenGL 3.2+ core profile.
* **devil.graphics** - Some helpful utilities for rendering, like a camera class and mesh drawer.
* **devil.gui** - Basic wrappers for STB and Nuklear, allowing for TrueType font loading and GUIs.
* **devil.io** - A grab bag of fairly useless buffer and stream utilities.
* **devil.math** - A mature, fast linear algebra library with vectors, matrices, and quaternia. [Compare to GLM.](http://glm.g-truc.net/)
* **devil.model** - Allows loading of Blender's .blend files, with support for meshes, skeletal animations, materials, and more.
* **devil.phys** - Contains ActorDriver, which allows for character movement and collision in 3D mesh environments.
* **devil.util** - A few generic Java utilities.
