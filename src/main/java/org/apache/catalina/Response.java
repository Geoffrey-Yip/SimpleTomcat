package org.apache.catalina;


import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Response接口
 * Tomcat4精简版
 */
public interface Response {

    Connector getConnector();


    /**
     * Set the Connector through which this Response is returned.
     *
     * @param connector The new connector
     */
    void setConnector(Connector connector);


    /**
     * Return the number of bytes actually written to the output stream.
     */
    int getContentCount();


    /**
     * Set the application commit flag.
     *
     * @param appCommitted The new application committed flag value
     */
    void setAppCommitted(boolean appCommitted);


    /**
     * Application commit flag accessor.
     */
    boolean isAppCommitted();


    /**
     * Return the "processing inside an include" flag.
     */
    boolean getIncluded();


    /**
     * Set the "processing inside an include" flag.
     *
     * @param included <code>true</code> if we are currently inside a
     *                 RequestDispatcher.include(), else <code>false</code>
     */
    void setIncluded(boolean included);


    /**
     * Return descriptive information about this Response implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    String getInfo();


    /**
     * Return the Request with which this Response is associated.
     */
    Request getRequest();


    /**
     * Set the Request with which this Response is associated.
     *
     * @param request The new associated request
     */
    void setRequest(Request request);


    /**
     * Return the <code>ServletResponse</code> for which this object
     * is the facade.
     */
    ServletResponse getResponse();


    /**
     * Return the output stream associated with this Response.
     */
    OutputStream getStream();


    /**
     * Set the output stream associated with this Response.
     *
     * @param stream The new output stream
     */
    void setStream(OutputStream stream);


    /**
     * Set the suspended flag.
     *
     * @param suspended The new suspended flag value
     */
    void setSuspended(boolean suspended);


    /**
     * Suspended flag accessor.
     */
    boolean isSuspended();


    /**
     * Set the error flag.
     */
    void setError();


    /**
     * Error flag accessor.
     */
    boolean isError();


    /**
     * Create and return a ServletOutputStream to write the content
     * associated with this Response.
     *
     * @throws IOException if an input/output error occurs
     */
    ServletOutputStream createOutputStream() throws IOException;


    /**
     * Perform whatever actions are required to flush and close the output
     * stream or writer, in a single operation.
     *
     * @throws IOException if an input/output error occurs
     */
    void finishResponse() throws IOException;


    /**
     * Return the content length that was set or calculated for this Response.
     */
    int getContentLength();


    /**
     * Return the content type that was set or calculated for this response,
     * or <code>null</code> if no content type was set.
     */
    String getContentType();


    /**
     * Return a PrintWriter that can be used to render error messages,
     * regardless of whether a stream or writer has already been acquired.
     *
     * @return Writer which can be used for error reports. If the response is
     * not an error report returned using sendError or triggered by an
     * unexpected exception thrown during the servlet processing
     * (and only in that case), null will be returned if the response stream
     * has already been used.
     */
    PrintWriter getReporter();


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    void recycle();


    /**
     * Reset the data buffer but not any status or header information.
     */
    void resetBuffer();


    /**
     * Send an acknowledgment of a request.
     *
     * @throws IOException if an input/output error occurs
     */
    void sendAcknowledgement() throws IOException;
}
