package fr.cs.orekit.iers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import fr.cs.orekit.errors.OrekitException;


/** Base class for IERS files crawlers.
 * @see IERSDirectoryCrawler#crawl(IERSFileCrawler)
 * @author Luc Maisonobe
 */
public abstract class IERSFileCrawler {

    /** Current file. */
    private File file;

    /** File name pattern. */
    private final Pattern supportedFilesPattern;

    /** Simple constructor.
     * @param supportedFilesPattern file name pattern for supported files
     */
    protected IERSFileCrawler(final String supportedFilesPattern) {
        this.supportedFilesPattern = Pattern.compile(supportedFilesPattern);
    }

    /** Get the current file.
     * @return current file
     */
    protected File getFile() {
        return file;
    }

    /** Check if a file is supported.
     * @param fileName file to check
     * @return true if file name correspond to a supported file
     */
    public boolean fileIsSupported(final String fileName) {
        return supportedFilesPattern.matcher(fileName).matches();
    }

    /** Visit a file.
     * @param file file to visit
     * @exception OrekitException if some data is missing, can't be read
     * or if some loader specific error occurs
     */
    public void visit(// CHECKSTYLE: stop HiddenField check
                      final File file
                      // CHECKSTYLE: resume HiddenField check
                      )
        throws OrekitException {
        BufferedReader reader = null;
        try {
            this.file = file;
            InputStream is = new FileInputStream(file);
            if (file.getName().endsWith(".gz")) {
                // add the decompression filter
                is = new GZIPInputStream(is);
            }
            reader = new BufferedReader(new InputStreamReader(is));
            visit(reader);
            reader.close();
        } catch (IOException ioe) {
            throw new OrekitException(ioe.getMessage(), ioe);
        } catch (ParseException pe) {
            throw new OrekitException(pe.getMessage(), pe);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    throw new OrekitException(ioe.getMessage(), ioe);
                }
            }
        }
    }

    /** Visit a file from a reader.
     * @param reader data stream reader
     * @exception IOException if data can't be read
     * @exception ParseException if data can't be parsed
     * @exception OrekitException if some data is missing
     * or if some loader specific error occurs
     */
    protected abstract void visit(BufferedReader reader)
        throws IOException, ParseException, OrekitException;

}
