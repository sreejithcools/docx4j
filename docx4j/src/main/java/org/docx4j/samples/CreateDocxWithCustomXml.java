/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.samples;

import java.io.File;

import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Creates a WordprocessingML document from scratch. 
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class CreateDocxWithCustomXml {

	public static void main(String[] args) throws Exception {
		
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		wordMLPackage.getMainDocumentPart()
			.addStyledParagraphOfText("Title", "Hello world");

		wordMLPackage.getMainDocumentPart().addParagraphOfText("from docx4j!");
		
		// To get bold text, you must set the run's rPr@w:b,
	    // so you can't use the createParagraphOfText convenience method

		//org.docx4j.wml.P p = wordMLPackage.getMainDocumentPart().createParagraphOfText("text");
		
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
		org.docx4j.wml.P  p = factory.createP();

		org.docx4j.wml.Text  t = factory.createText();
		t.setValue("text");

		org.docx4j.wml.R  run = factory.createR();
		run.getRunContent().add(t);		
		
		p.getParagraphContent().add(run);
		
		
		org.docx4j.wml.RPr rpr = factory.createRPr();		
		org.docx4j.wml.BooleanDefaultTrue b = new org.docx4j.wml.BooleanDefaultTrue();
	    b.setVal(true);	    
	    rpr.setB(b);
	    
		run.setRPr(rpr);
		
		// Optionally, set pPr/rPr@w:b		
	    org.docx4j.wml.PPr ppr = factory.createPPr();	    
	    p.setPPr( ppr );
	    org.docx4j.wml.ParaRPr paraRpr = factory.createParaRPr();
	    ppr.setRPr(paraRpr);	    
	    rpr.setB(b);
	    
	            
	    wordMLPackage.getMainDocumentPart().addObject(p);
	    
	    
	    // Here is an easier way:
	    String str = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" ><w:r><w:rPr><w:b /></w:rPr><w:t>Bold, just at w:r level</w:t></w:r></w:p>";
	    
	    wordMLPackage.getMainDocumentPart().addObject(
	    			org.docx4j.XmlUtils.unmarshalString(str) );
	    			
		//injectCustomXmlDataStoragePart(wordMLPackage.getMainDocumentPart());
		injectCustomXmlDataStoragePart(wordMLPackage);
		
		// Now save it 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/customxml2.docx") );
		
		System.out.println("Done.");
				
	}
	
	/* Currently (2009 02 13), docx4j *requires* that if a part has the main document part as its source 
	 * part, then its name should be relative to that part.
	 * 
	 * In other words, /word/customXML/item1.xml rather than /customXML/item1.xml,
	 * which gives:
	 * 
13.02.2009 14:50:46 *INFO * SaveToZipFile: For Relationship Id=rId1 Source is /word/document.xml, Target is customXML/item1.xml (SaveToZipFile.java, line 247)
13.02.2009 14:50:46 *INFO * SaveToZipFile: Getting part /word/customXML/item1.xml (SaveToZipFile.java, line 287)
13.02.2009 14:50:46 *DEBUG* PartName: Trying to create part name /word/customXML/item1.xml (PartName.java, line 150)
13.02.2009 14:50:46 *DEBUG* PartName: /word/customXML/item1.xml part name created. (PartName.java, line 170)
13.02.2009 14:50:46 *ERROR* SaveToZipFile: Part word/customXML/item1.xml not found! (SaveToZipFile.java, line 292)
	 * 
	 * Note that if you open a docx containing /word/customXML/item1.xml in Word, and re-save it, then
	 * resulting docx will contain customXml/item1.xml (and it will also create itemProps1.xml etc).
	 * The rel has target "../customXml/item1.xml
	 * 
	 * If you use the WordML package (as opposed to the main document part) as the source to which
	 * you attach the custom xml part, note that when you re-save the document in Word 2007, 
	 * Word will drop the custom xml part entirely!!
	 * 
	 */
	
	
	public static void injectCustomXmlDataStoragePart(Base base) {
		
		try {
			org.docx4j.openpackaging.parts.CustomXmlDataStoragePart customXmlDataStoragePart = 
//				new org.docx4j.openpackaging.parts.CustomXmlDataStoragePart(new PartName("/word/customXML/item1.xml"));
				new org.docx4j.openpackaging.parts.CustomXmlDataStoragePart(new PartName("/customXML/item1.xml"));
			
			customXmlDataStoragePart.setDocument( createCustomXmlDocument() );
					
			base.addTargetPart(customXmlDataStoragePart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static Document createCustomXmlDocument() {
		
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");
		
		Element myChild = root.addElement("myChild").addAttribute("att1", "att1Val").addAttribute("att2", "att2Val").addText("some text");
		
		return document;
		
	}
	
}