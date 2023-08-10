# Overview 

This directory contains resources that attributed to projects other than Autopsy. 

# Folders

The `thirdparty` directory contains the following directories:
- [`build`](./build/): Any resources necessary to build Autopsy and tools.
- [`main`](./main/): Any resoures utilized by Autopsy at runtime.
- [`tools`](./tools/): Any projects utilized by any tools.

# Third Party Folder structure

Each subfolder in one of the [immediate folders](#folders) has the following structure.  Any of these files and directories are optional.
- `README.md`: provides information like the project name, source url, and current version used.
- `build.xml`: This provides targets for building the dependency from source as well as possibly providing any `PATH` or library paths through the `build` target.
- `src`: Location for any source code to be used during build.
- `doc`: Any documentation on the project.
- `dist`: Location of files to be directly utilized by Autopsy or tools.
  - `resources`: The contents of this folder will be placed directly into the `autopsy` directory of the Autopsy build (i.e. `<AUTOPSY_INSTALL>/autopsy/<subfolder name>`).
  - `ext`: This includes any java jar files to be added to the classpath.
  - `bin`: This includes any OS/arch-dependent files for the purposes of execution.
  - `lib`: Contains OS/arch-dependent libs.  This gets placed in the relevant `lib` folder.
    - `<os>_<arch>`: Folder underneath both `bin` and `lib`.  This includes resources for a specific os and architecture.  OS value could be `windows`, `unix` (used for linux), or `mac`. Currently, the supported arch value is `amd64`.  These will be placed in the folder `<AUTOPSY_INSTALL>/autopsy/<subfolder name>`.