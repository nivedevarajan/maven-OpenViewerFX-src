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
 * GUIAnnotationPanel.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.generic;

import java.awt.Color;
import org.jpedal.PdfDecoderInt;

public interface GUIAnnotationPanel {
    
    boolean addPanel();
    
    
    Object getAnnotationType();
    
    void clearAnnotationType();
    
    
    void addAnnotationForWriting(Object annot);
    
    void addUnsavedAnnotationsToPage(PdfDecoderInt decode_pdf);

    void addAnnotationToDisplay(PdfDecoderInt decode_pdf, int[] types, Color[] cols, Object[] objs);
    
    boolean annotationAdded();

    void clearAnnotations();
    
    void saveAnnotations(String input, String output);
    
    void saveForms(String input, String output, Object[] objArr);
    
    void dispose();
    
    Object getDisplayPanel();
}
