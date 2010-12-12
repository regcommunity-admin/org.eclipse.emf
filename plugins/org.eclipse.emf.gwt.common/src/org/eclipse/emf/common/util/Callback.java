/**
 * <copyright>
 *
 * Copyright (c) 2010 Ed Merks and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Ed Merks - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: Callback.java,v 1.1 2010/12/12 20:29:31 emerks Exp $
 */
package org.eclipse.emf.common.util;

public interface Callback<T>
{
  void onFailure(Throwable caught);
  void onSuccess(T result);
}
