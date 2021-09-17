**please just use another encryption software which works correctly and has better en-/decoding options**

**_cryptoGX_** - just another en- / decryption software

- [Introduction](#introduction)
- [Downloads](#Downloads)
- [Usage](#Usage)
- [License](#License)

# Introduction

**cryptoGX** is a java based software for en- / decrypting texts or files and secure delete files.
It was designed to be controlled with a GUI, but can also be used from the command line.

Because the GUI uses javaFX, java version from 1.8.0_40 to 10.* with javaFX support is required.
Alternatively you can compile the [source code](#https://github.com/blueShard-dev/cryptoGX/archive/master.zip) with higher
java version and include [javaFX](#https://openjfx.io/) manually

# Downloads

### [All releases](#https://github.com/blueShard-dev/cryptoGX/releases/)

| Latest release (1.12.0) |
|:------------------------|
| [Source code](https://github.com/blueShard-dev/cryptoGX/archive/v1.12.0.zip) |
| [Executable jar file](https://github.com/blueShard-dev/cryptoGX/releases/download/v1.12.0/cryptoGX.jar) |
| [Windows installer](https://github.com/blueShard-dev/cryptoGX/releases/download/v1.12.0/cryptoGX-1.12.0_win_installer.exe) |
| [Debian installer](https://github.com/blueShard-dev/cryptoGX/releases/download/v1.12.0/cryptogx-1.12.0_debian_installer.deb) |

**NOTE**: If you download one of the windows executables (`.exe`) your antivirus may scan the file(s)

# Usage

The `.jar` file can be started with and without arguments.
If it gets called without arguments the GUI will launch normaly.
If it gets called with arguments, the GUI won't launch. The usage of arguments are described below.

Command line arguments:
```
Usage AES:

    Text en- / decryption
        encrypt: <cryptoGX jar file> AES <key> <salt> encrypt <string>
        decrypt: <cryptoGX jar file> AES <key> <salt> decrypt <encrypted string>

    File en- / decryption
        encrypt: <cryptoGX jar file> AES <key> <salt> encrypt <path of file to encrypt> <encrypted file dest>
        decrypt: <cryptoGX jar file> AES <key> <salt> decrypt <encrypted file path> <decrypted file dest>

File secure delete: <cryptoGX jar file> delete <iterations> <path of file to delete>        //for <iterations> the argument 'default' can be used, which is 5
```
or type `<cryptoGX jar file> help` to get help

# License

This project is licensed under the Mozilla Public Licence 2.0 - see the [LICENSE](LICENSE) file for more details
