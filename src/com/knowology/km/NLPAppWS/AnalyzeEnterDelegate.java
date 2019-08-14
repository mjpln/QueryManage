package com.knowology.km.NLPAppWS;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.3-hudson-390-
 * Generated source version: 2.0
 * 
 */
@WebService(name = "AnalyzeEnterDelegate", targetNamespace = "http://knowology.com/")
public interface AnalyzeEnterDelegate {

	/**
	 * 
	 * @param arg0
	 * @return returns java.lang.String
	 */
	@WebMethod(operationName = "Analyze")
	@WebResult(targetNamespace = "")
	@RequestWrapper(localName = "Analyze", targetNamespace = "http://knowology.com/", className = "com.knowology.km.NLPAppWS.Analyze")
	@ResponseWrapper(localName = "AnalyzeResponse", targetNamespace = "http://knowology.com/", className = "com.knowology.km.NLPAppWS.AnalyzeResponse")
	public String analyze(
			@WebParam(name = "arg0", targetNamespace = "") String arg0);

	/**
	 * 
	 * @param arg0
	 * @return returns java.lang.String
	 */
	@WebMethod(operationName = "FormatAnaluze")
	@WebResult(targetNamespace = "")
	@RequestWrapper(localName = "FormatAnaluze", targetNamespace = "http://knowology.com/", className = "com.knowology.km.NLPAppWS.FormatAnaluze")
	@ResponseWrapper(localName = "FormatAnaluzeResponse", targetNamespace = "http://knowology.com/", className = "com.knowology.km.NLPAppWS.FormatAnaluzeResponse")
	public String formatAnaluze(
			@WebParam(name = "arg0", targetNamespace = "") String arg0);

	/**
	 * 
	 * @return returns boolean
	 */
	@WebMethod(operationName = "UpdateProcessController")
	@WebResult(targetNamespace = "")
	@RequestWrapper(localName = "UpdateProcessController", targetNamespace = "http://knowology.com/", className = "com.knowology.km.NLPAppWS.UpdateProcessController")
	@ResponseWrapper(localName = "UpdateProcessControllerResponse", targetNamespace = "http://knowology.com/", className = "com.knowology.km.NLPAppWS.UpdateProcessControllerResponse")
	public boolean updateProcessController();

}
