# unzip-extension

An unzip extension step for XML Calabash that unzips whole archives.
It is particularly useful if you want to access zip contents as
extracted files (and don't want to retrieve single files recursively,
and potentially base64 encoded).

Usage example:

    java -cp "/path/to/calabash.jar:/path/to/unzip-extension/" com.xmlcalabash.drivers.Main -c file:///uri/of/transpect-config.xml unzip-example.xpl

Configure tr:unzip step by passing options to it:

 * zip (required):      file name (not URI) of a zip file
 * dest-dir (required): directory (relative or absolute; not an URI)
 * overwrite ('yes'/'no', optional, default 'no'): delete dest-dir (if it exists) prior to unzip 
 * file (optional):     file name relative to zip root. If omitted, extract complete zip file

The directory (and missing subdirectories) will be created if missing.
