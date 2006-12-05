/**
 * <copyright>
 *
 * Copyright (c) 2003-2006 IBM Corporation and others.
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
 * $Id: EMOFHelperImpl.java,v 1.12 2006/12/05 20:23:28 emerks Exp $
 */
package org.eclipse.emf.ecore.xmi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.ECrossReferenceEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;


public class EMOFHelperImpl extends XMLHelperImpl implements EMOFHandler.Helper
{
  protected EClass propertyClass = null;
  protected List<EObject> propertyFeatureList = null;

  public EMOFHelperImpl(XMLResource resource)
  {
    super(resource);
    packages.put(EcorePackage.eINSTANCE, EMOFExtendedMetaData.EMOF_PACKAGE_NS_PREFIX);
  }

  @Override
  public Object getValue(EObject object, EStructuralFeature feature)
  {
    if (feature == EcorePackage.eINSTANCE.getEStructuralFeature_Changeable())
    {
      return ((EStructuralFeature)object).isChangeable() ? Boolean.FALSE : Boolean.TRUE; // EMOF.isReadOnly = !Ecore.changeable
    }
    else
    {
      return super.getValue(object, feature);
    }
  }

  @Override
  public void setValue(EObject object, EStructuralFeature feature, Object value, int position)
  {
    if (feature == EcorePackage.eINSTANCE.getEStructuralFeature_Changeable())
    {
      ((EStructuralFeature)object).setChangeable(Boolean.FALSE.toString().equals(value)); // Ecore.changeable = !EMOF.isReadOnly
    }
    else if (feature == EcorePackage.eINSTANCE.getETypedElement_UpperBound() && "*".equals(value))
    {
      ((ETypedElement)object).setUpperBound(-1);
    }
    else
    {
      super.setValue(object, feature, value, position);
    }
  }

  @Override
  public String getHREF(EObject obj)
  {
    String href = super.getHREF(obj);
    if (href != null && href.startsWith(EMOFExtendedMetaData.ECORE_EDATATYPE_HREF_PREFIX))
    {
      String dataType = href.substring(EMOFExtendedMetaData.ECORE_EDATATYPE_HREF_PREFIX.length());
      for (int i = 0; i < EMOFExtendedMetaData.MAPPED_ECORE_EDATATYPES.length; i++)
      {
        if (dataType.equals(EMOFExtendedMetaData.MAPPED_ECORE_EDATATYPES[i]))
        {
          return EMOFExtendedMetaData.MAPPED_EMOF_EDATATYPE_HREF_PREFIX + EMOFExtendedMetaData.MAPPED_EMOF_EDATATYPES[i];
        }
      }
      return EMOFExtendedMetaData.UNMAPPED_EMOF_EDATATYPE_HREF_PREFIX + dataType;
    }

    return href;
  }

  @Override
  public EStructuralFeature getFeature(EClass eClass, String namespaceURI, String name, boolean isElement)
  {
    if (eClass == EcorePackage.eINSTANCE.getEAnnotation() && EMOFExtendedMetaData.EMOF_TAG_ELEMENT.equals(name))
    {
      return EcorePackage.eINSTANCE.getEAnnotation_References();
    }

    for (int i = 0, size = eClass.getFeatureCount(); i < size; ++i)
    {
      EStructuralFeature eStructuralFeature = eClass.getEStructuralFeature(i);
      if (name.equals(extendedMetaData.getName(eStructuralFeature)) &&
            (namespaceURI == null ? 
             extendedMetaData.getNamespace(eStructuralFeature) == null : 
             namespaceURI.equals(extendedMetaData.getNamespace(eStructuralFeature))))
      {
        computeFeatureKind(eStructuralFeature);
        return eStructuralFeature;
      }
    }

    return null;
  }

  @Override
  public EClassifier getType(EFactory eFactory, String typeName)
  {
    EPackage ePackage = eFactory.getEPackage();
    if (ePackage == EcorePackage.eINSTANCE)
    {
      if (EcorePackage.Literals.ESTRUCTURAL_FEATURE.getName().equals(typeName))
      {
        if (propertyClass == null)
        {
          propertyClass = EcoreFactory.eINSTANCE.createEClass();
          propertyClass.getESuperTypes().add(EcorePackage.Literals.EREFERENCE);
          propertyClass.getESuperTypes().add(EcorePackage.Literals.EATTRIBUTE);
          propertyClass.setName("EMOFProperty");

          EPackage propertyPackage = EcoreFactory.eINSTANCE.createEPackage();
          propertyPackage.getEClassifiers().add(propertyClass);

          propertyFeatureList = new ArrayList<EObject>();
        }
        return propertyClass;
      }
      else if (EMOFExtendedMetaData.TAG.equals(typeName))
      {
        return EcorePackage.eINSTANCE.getEAnnotation();
      }
    }
    return super.getType(eFactory, typeName);
  }
  
  @Override
  public EObject createObject(EFactory eFactory, EClassifier type)
  {
    if (type == propertyClass)
    {
      EObject property = propertyClass.getEPackage().getEFactoryInstance().create(propertyClass);
      propertyFeatureList.add(property);
      return property;
    }
    else if (EcorePackage.Literals.EANNOTATION == type)
    {
      EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
      annotation.setSource(EMOFExtendedMetaData.EMOF_PACKAGE_NS_URI);
      return annotation;
    }
    return super.createObject(eFactory, type);
  }

  public void convertPropertyFeatures()
  {
    if (propertyFeatureList != null)
    {
      EcoreUtil.Copier copier = 
        new EcoreUtil.Copier(false)
        {
          private static final long serialVersionUID = 1L;

          @Override
          protected EObject createCopy(EObject eObject)
          {
            EClass eClass = 
              ((EStructuralFeature)eObject).getEType() instanceof EDataType
                ? EcorePackage.eINSTANCE.getEAttribute()
                : EcorePackage.eINSTANCE.getEReference();
            return EcoreUtil.create(eClass);
          }
          @Override
          protected void copyContainment(EReference eReference, EObject eObject, EObject copyEObject)
          {
            // eAnnotations is the only possible containment reference. We'll move the annotations instead of copy
            ((EStructuralFeature)copyEObject).getEAnnotations().addAll(((EStructuralFeature)eObject).getEAnnotations());
          }
        };
      copier.copyAll(propertyFeatureList);
      copier.copyReferences();

      for (Map.Entry<EObject, EObject> entry : copier.entrySet())
      {
        EStructuralFeature emofFeature = (EStructuralFeature)entry.getKey();
        EStructuralFeature ecoreFeature = (EStructuralFeature)entry.getValue();
        EClass eClass = emofFeature.getEContainingClass();
        eClass.getEStructuralFeatures().set(eClass.getEStructuralFeatures().indexOf(emofFeature), ecoreFeature);
      }

      for (TreeIterator<Notifier> contents = EcoreUtil.getAllContents(resource.getContents(), false); contents.hasNext(); )
      {
        EObject eObject = (EObject)contents.next();
        for (EContentsEList.FeatureIterator<EObject> featureIterator = 
               new ECrossReferenceEList.FeatureIteratorImpl<EObject>(eObject) 
               {
                 @Override
                protected boolean isIncluded(EStructuralFeature eStructuralFeature)
                {
                  return !eStructuralFeature.isDerived() && super.isIncluded(eStructuralFeature);
                }
               };
             featureIterator.hasNext(); )
        {
          EObject targetEObject = featureIterator.next();
          EObject copyEObject = copier.get(targetEObject);
          if (copyEObject != null)
          {
            EReference eReference = (EReference)featureIterator.feature();
            if (eReference.isMany())
            {
              @SuppressWarnings("unchecked") List<EObject> list = (List<EObject>)eObject.eGet(eReference);
              list.set(list.indexOf(targetEObject), copyEObject);
            }
            else
            {
              eObject.eSet(eReference, copyEObject);
            }
          }
        }
      }
    }
  }
}
