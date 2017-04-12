# unzip-extension

An unzip extension step for XML Calabash that unzips entire archives.

## description 

XProc 1.0 lacks of a native unzip solution. Therefore, XProc processors such as 
XML Calabash or Morgana provide own extension steps for extracting zip archives. 
However, these steps allow only to extract XML files in an archive. To extract other 
file types, we've developed this unzip-extension step for XML calabash. It extracts 
all files to the specified location and provides an XML data set as output.

## include in XML Calabash

1. add the unzip-extension to your XML Calabash `$XPROC-CONFIG` file

```
<xproc-config xmlns="http://xmlcalabash.com/ns/configuration"
  xmlns:tr="http://transpect.io"
  xmlns:tr-internal="http://transpect.io/internal">

  <implementation type="tr-internal:unzip"                class-name="io.transpect.calabash.extensions.UnZip"/>
  
</xproc-config>

```

2. add `io/transpect/calabash/extensions/UnZip.class` to your Java `$CLASSPATH` (otherwise XML Calabash will fail with a class not found error)

3. run XML Calabash (note that )

```
java \
   -cp "$CLASSPATH" \
   -Dfile.encoding=UTF-8 \
   -Dxml.catalog.files=$CATALOG \
   -Dxml.catalog.staticCatalog=1 \
   com.xmlcalabash.drivers.Main \
   -Xtransparent-json \
   -E org.xmlresolver.Resolver \
   -U org.xmlresolver.Resolver \
   -c $XPROC-CONFIG \
   unzip-example.xpl \
   zip=myarchive.zip \
   path=output
```




## compile

We already provide a class file and a jar file, but feel free to compile this extension for your needs.

1. add the jars of XML Calabash and Saxon to your Java `$CLASSPATH`
2. compile it

```
javac -source 1.7 -target 1.7 -d . -cp $CLASSPATH src/main/java/io/transpect/calabash/extensions/UnZip.java
```
