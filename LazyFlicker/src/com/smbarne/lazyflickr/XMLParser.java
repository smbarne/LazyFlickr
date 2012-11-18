package com.smbarne.lazyflickr;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * XMLParser is based on code from
 * http://www.androidhive.info/2011/11/android-xml-parsing-tutorial/
 * 
 * Several additions and refinements were made and noted throughout the code.
 */
public class XMLParser {
	
	public String getXmlFromUrl(String url) {
        String xml = null;
 
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
 
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // TODO: Toast notification on can't connect -SB

        return xml;
    }
	
	public Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
 
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
 
            } catch (ParserConfigurationException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (SAXException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }

        return doc;
    }
	
	public String getValue(Element item, String str) {
	    NodeList n = item.getElementsByTagName(str);
	    return this.getElementValue(n.item(0));
	}
	
	/**
	 * A method that returns the value of an attribute from an XML element.  Added to the
	 * original codebase 11/16/12 - SB.
	 * 
	 * @param item	The Parent XML element.
	 * @param elementName	The name of the element with the attribute desired. 
	 * @param attributeName	The name of the attribute.
	 * @return	Returns the string value of the attribute.  If the attribute cannot be found,
	 * 			an empty string is returned.
	 */
	public String getAttributeValue(Element item, String elementName, String attributeName) {
		NodeList n = item.getElementsByTagName(elementName);
	    if(n.getLength() > 0 && n.item(0).hasAttributes()) {
            NamedNodeMap attr_id = n.item(0).getAttributes();
            return attr_id.getNamedItem(attributeName).getTextContent();
        }
		
	    return "";
	}
	 
	public final String getElementValue( Node elem ) {
	         Node child;
	         if( elem != null){
	             if (elem.hasChildNodes()){
	                 for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
	                     if( child.getNodeType() == Node.TEXT_NODE  ){
	                         return child.getNodeValue();
	                     }
	                 }
	             }
	         }
	         return "";
	  } 
	
	public final Node getElement( Node elem ) {
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.hasAttributes()){
                        return child;
                    }
                }
            }
        }
        return null;
 } 
}
