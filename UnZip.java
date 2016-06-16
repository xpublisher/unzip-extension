/**
 * Unzip extension for non-XML files
 *
 * @author Lars Wittmar -- le-tex publishing services GmbH
 * @date   2012-03-08
 */

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;
import java.io.StringReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Axis;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.io.File;
import java.util.zip.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.xmlbeam.*;
import org.xmlbeam.dom.*;
import org.xmlbeam.config.*;

public class UnZip extends DefaultStep
{
	public UnZip(XProcRuntime runtime, XAtomicStep step)
    {
        super(runtime,step);
    }

    @Override
    public void setOutput(String port, WritablePipe pipe)
    {
        myResult = pipe;
    }

    @Override
    public void reset()
    {
        myResult.resetWriter();
    }

    @Override
    public void run()
        throws SaxonApiException
    {
        super.run();
        
        String result, source = null;
        File folder = null;
        try {
			source = getOption(new QName("zip")).getString();
			String destination = getOption(new QName("dest-dir")).getString();
            String removeS = getOption(new QName("overwrite")).getString();
            boolean remove = false; 
            if (removeS != null) {
                if (removeS.matches("yes")) {
                    remove = true;
                }
            }

			String filename = "";
			if (getOption(new QName("file"))!=null) {
                filename = getOption(new QName("file")).getString();
			}
			
			folder = new File(destination);
            if (remove) {
            //     FileUtils.deleteDirectory(folder);
            }
			if(! folder.exists()) { 
                folder.mkdirs(); 
            }
			final int BUFFER = 2048;
			BufferedOutputStream dest = null;
			FileInputStream fis = new FileInputStream(source);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry;

			DefaultXMLFactoriesConfig config = new DefaultXMLFactoriesConfig();
			config.createNameSpaceMapping().add("c", "http://www.w3.org/ns/xproc-step");
			DOMAccess domAccess = new XBProjector(config).projectEmptyDocument(DOMAccess.class);
//			String xpath="c:files [@xml:base='"+folder.toURI()+"']";
			List<String> fileEntries = new ArrayList<String>();
				
            int numFiles = 0;
			result = "";
//			result = "<c:files xmlns:c=\"http://www.w3.org/ns/xproc-step\" xml:base=\"" + folder.toURI() + "\">";
			//without given fileNode extract everything
			if(filename.equals("")) {
                while((entry = zis.getNextEntry()) != null) {
                    int count;
                    byte data[] = new byte[BUFFER];			
				
                    if(entry.isDirectory()) {
                        File dir = new File (destination + "/" + entry);

						fileEntries.add(entry.getName());
//                        result += "<c:directory name=\"" + entry.getName() + "\"/>";
                        if (!dir.exists()) { dir.mkdirs(); }
                    } else {
                        String destname = destination + "/" + entry.getName();
                        String destdirname = destname.replaceAll("[^/]+$", "");
                        File destdir = new File(destdirname);
                        if (!destdir.exists()) { destdir.mkdirs(); }
                        FileOutputStream fos = new FileOutputStream(destname);
                        numFiles++;

						fileEntries.add(entry.getName());
//						result += "<c:file name=\"" + entry.getName() + "\"/>";
                        dest = new BufferedOutputStream(fos, BUFFER);
                        while ((count = zis.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        dest.close();
                    }
                }
			} else {
                while((entry = zis.getNextEntry()) != null) {
                    if(filename.equals(entry.getName())) {
                        int count;
                        byte data[] = new byte[BUFFER];
						
                        String destname = destination + "/" + entry.getName();
                        String destdirname = destname.replaceAll("[^/]+$", "");
                        File destdir = new File(destdirname);
                        if (!destdir.exists()) { destdir.mkdirs(); }
                        FileOutputStream fos = new FileOutputStream(destination + "/" + entry.getName());
                        numFiles++;
						fileEntries.add(entry.getName());
//                        result += "<c:file name=\"" + entry.getName() + "\"/>";
                        dest = new BufferedOutputStream(fos, BUFFER);
                        while ((count = zis.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        dest.close();
                    }							 
                }
			}

			//remove extra dir
			Collections.sort(fileEntries);//sort from short to long path
			for(int a = 0; a < fileEntries.size() - 1; a++) {
				if(fileEntries.get(a+1).indexOf(fileEntries.get(a)) != -1) {
					fileEntries.remove(a);
				}
			}
			
			for (String ent:fileEntries) {
			String xpath="c:files";
			
			String[] parts = ent.split("\\/");
			for(int i = 0; i <= parts.length - 1; i++) {
				if(i == (parts.length - 1)) {
					if(parts[i].indexOf(".") != -1) {
						xpath+="/c:file [@name='"+parts[i]+"']";
					} else {
						xpath+="/c:directory [@name='"+parts[i]+"']";				
					}
				} else {
					xpath+="/c:directory [@name='"+parts[i]+"']";				
				}
			}
			domAccess.create(xpath, "");
		}
		result = domAccess.asString();
			
			zis.close();
			if (numFiles == 0) {
			  result = "<c:error xmlns:c=\"http://www.w3.org/ns/xproc-step\" xmlns:letex=\"http://www.le-tex.de/namespace\" code=\"zip-error\" href=\""+(folder.toURI()+source)+"\">No content processed. Zip file may be empty or corrupted.</c:error>";
			} else {
//			  result += "</c:files>";			
			}
            
		  } catch(Exception e) {
			result = "<c:error xmlns:c=\"http://www.w3.org/ns/xproc-step\" xmlns:letex=\"http://www.le-tex.de/namespace\" code=\"zip-error\" href=\""+(folder.toURI()+source)+"\">Zip file seems to be corrupted: "+e.getMessage()+"</c:error>";
		  }
        DocumentBuilder builder = runtime.getProcessor().newDocumentBuilder();
        Source src = new StreamSource(new StringReader(result));
        XdmNode doc = builder.build(src);

        myResult.write(doc);
    }

    private WritablePipe myResult = null;
	
}
