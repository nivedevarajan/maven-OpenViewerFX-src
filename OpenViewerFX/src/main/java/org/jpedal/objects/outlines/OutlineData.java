/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2016 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * OutlineData.java
 * ---------------
 */
package org.jpedal.objects.outlines;

import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.types.Array;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * encapsulate the Outline data
 */
public class OutlineData {

	private Document OutlineDataXML;

    private final Map<String, PdfObject> DestObjs=new HashMap<String, PdfObject>();

	/**create list when object initialised*/
	public OutlineData(){

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			OutlineDataXML=factory.newDocumentBuilder().newDocument();
		} catch (final ParserConfigurationException e) {
			System.err.println("Exception "+e+" generating XML document");
		}

		//increment so arrays correct size
		//pageCount++;

	}

	/**return the list*/
	public Document getList(){
		return OutlineDataXML;
	}

	/**
	 * read the outline data
	 */
	@SuppressWarnings("UnusedReturnValue")
    public int readOutlineFileMetadata(final PdfObject OutlinesObj, final PdfObjectReader currentPdfFile) {

		final int count=OutlinesObj.getInt(PdfDictionary.Count);

		final PdfObject FirstObj=OutlinesObj.getDictionary(PdfDictionary.First);
        currentPdfFile.checkResolved(FirstObj);
        if(FirstObj !=null){

			final Element root=OutlineDataXML.createElement("root");

			OutlineDataXML.appendChild(root);

			final int level=0;
			readOutlineLevel(root,currentPdfFile, FirstObj, level, false);

		}

		return count;
	}

	/**
	 * read a level
	 */
	private void readOutlineLevel(final Element root, final PdfObjectReader currentPdfFile, PdfObject outlineObj, final int level, boolean isClosed) {

		String ID;
		//float coord=0;
		int page;

		Element child=OutlineDataXML.createElement("title");

        PdfObject FirstObj=null, NextObj;

		PdfArrayIterator DestObj;

		while(true){

			if(FirstObj!=null) {
                outlineObj = FirstObj;
            }

			ID=outlineObj.getObjectRefAsString();

			//set to -1 as default
			//coord=-1;
			page=-1;

			/*
			 * process and move onto next value
			 */
			FirstObj=outlineObj.getDictionary(PdfDictionary.First);
            currentPdfFile.checkResolved(FirstObj);
            NextObj=outlineObj.getDictionary(PdfDictionary.Next);
            currentPdfFile.checkResolved(NextObj);

            final int numberOfItems=outlineObj.getInt(PdfDictionary.Count);

            if(numberOfItems!=0) {
                isClosed = numberOfItems < 0;
            }

            //get Dest from Dest or A object
			DestObj=outlineObj.getMixedArray(PdfDictionary.Dest);

            PdfObject Aobj=outlineObj;

            if(DestObj==null || DestObj.getTokenCount()==0){
				Aobj=outlineObj.getDictionary(PdfDictionary.A);

                //A can also have DEST as a D value (we convert it to DEST to simplify all our usage
                //so should not be set to null
				if(Aobj!=null){ //If there is an A object we will not encounter a Dest
						//DestObj=null; (will break files)
						DestObj=Aobj.getMixedArray(PdfDictionary.Dest);
				}
			}

			String ref=null;

        if (DestObj != null && DestObj.getTokenCount()>0){// && type==PdfDictionary.Goto) {

				int count=DestObj.getTokenCount();

                if(count>0){
					if(DestObj.isNextValueRef()) {
                        ref = DestObj.getNextValueAsString(true);
                    } else{ //its nameString name (name) linking to obj so read that
                        String nameString =DestObj.getNextValueAsString(true);

                        //check if object and read if so (can also be an indirect name which we lookup
                        if(nameString!=null ){

                            if(nameString.startsWith("/")){
                                nameString=nameString.substring(1);
                            }
                            
                            ref=currentPdfFile.convertNameToRef(nameString);

                            //allow for direct value
                            if(ref!=null && ref.startsWith("[")){

                                final byte[] raw=StringUtils.toBytes(ref);
                                final Array objDecoder=new Array(currentPdfFile.getObjectReader(),0, PdfDictionary.VALUE_IS_MIXED_ARRAY,raw);
                                objDecoder.readArray(Aobj, PdfDictionary.Dest);
                                DestObj=Aobj.getMixedArray(PdfDictionary.Dest);
                            }else if(ref!=null){
                            	Aobj=new OutlineObject(ref);
                                currentPdfFile.readObject(Aobj);
                                DestObj=Aobj.getMixedArray(PdfDictionary.Dest);
                            }

                            if(DestObj!=null){
                                count=DestObj.getTokenCount();

                                if(count>0 && DestObj.hasMoreTokens()) { //can be object or raw number
                                    if(DestObj.isNextValueRef()){
                                        ref = DestObj.getNextValueAsString(true);
                                    }else {
                                        ref=null;
                                        page=1+DestObj.getNextValueAsInteger(); //note it treats 0 as first page
                                    }
                                }
                            }
                        }
                    }
				}    				
			}

			if(ref!=null) {
                page = currentPdfFile.convertObjectToPageNumber(ref);
            }

			//add title to tree
			final byte[] titleData=outlineObj.getTextStreamValueAsByte(PdfDictionary.Title);
			if(titleData !=null){

				final String title= StringUtils.getTextString(titleData, false);

				//add node
				child=OutlineDataXML.createElement("title");
				root.appendChild(child);
				child.setAttribute("title",title);

			}

            child.setAttribute("isClosed", String.valueOf(isClosed));

            //store Dest so we can access
            if(Aobj!=null) {
                DestObjs.put(ID, Aobj);
            }

            if(page==PdfDictionary.Null){
                child.setAttribute("page", "-1");
            }else if(page!=-1) {
                child.setAttribute("page", String.valueOf(page));
            }

            child.setAttribute("level", String.valueOf(level));
            child.setAttribute("objectRef",ID);

			if(FirstObj!=null) {
                readOutlineLevel(child, currentPdfFile, FirstObj, level + 1, isClosed);
            }

			if(NextObj==null) {
                break;
            }

			FirstObj=NextObj;

		}
	}

    /**
     * not recommended for general usage
     * @param ref
     * @return Aobj
     */
    public PdfObject getAobj(final String ref) {
        return DestObjs.get(ref);
    }
}
