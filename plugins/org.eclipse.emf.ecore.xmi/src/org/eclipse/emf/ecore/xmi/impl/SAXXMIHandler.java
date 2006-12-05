/**
 * <copyright>
 *
 * Copyright (c) 2002-2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: SAXXMIHandler.java,v 1.6 2006/12/05 20:23:28 emerks Exp $
 */
package org.eclipse.emf.ecore.xmi.impl;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;


/**
 * This class is a SAX handler for creating MOF2 objects from an XMI 2.0 file.
 */
public class SAXXMIHandler extends XMIHandler
{

  /**
   * Constructor.
   */
  public SAXXMIHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options)
  {
    super(xmiResource, helper, options);
  }
  
  @Override
  protected String getXSIType()
  {
    String xsiType = attribs.getValue(TYPE_ATTRIB);

    if (xsiType == null)
    {
      xsiType = attribs.getValue(XMI_TYPE_ATTRIB);
    }

    return xsiType;
  }

  /**
   * Process the XMI attributes for the newly created object.
   */
  @Override
  protected void handleObjectAttribs(EObject obj)
  {
    if (attribs != null)
    {
      InternalEObject internalEObject = (InternalEObject)obj;
      for (int i = 0, size = attribs.getLength(); i < size; ++i)
      {
        String name = attribs.getQName(i);
        if (name.equals(ID_ATTRIB))
        {
          xmlResource.setID(internalEObject, attribs.getValue(i));
        }
        else if (name.equals(hrefAttribute) && (!recordUnknownFeature || types.peek() != UNKNOWN_FEATURE_TYPE))
        {
          handleProxy(internalEObject, attribs.getValue(i));
        }
        else if (!name.startsWith(XMLResource.XML_NS) && !notFeatures.contains(name))
        {
          setAttribValue(obj, name, attribs.getValue(i));
        }
      }
    }
  }
} // SAXXMIHandler
