/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */
package com.servoy.extensions.plugins.http;

import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;

/**
 * gives the Servoy developer access to some Cookie methods for basic cookie operations
 * 
 * @author paul
 */
public class Cookie implements IReturnedTypesProvider, IScriptable
{

	private org.apache.http.cookie.Cookie cookie;

	public Cookie()
	{
	}

	public void setCookie(org.apache.http.cookie.Cookie cookie)
	{
		this.cookie = cookie;
	}

	public Cookie(org.apache.http.cookie.Cookie cookie)
	{
		this.cookie = cookie;
	}

	/**
	 * Returns the cookie name.
	 *
	 * @sample
	 * var cookie = client.getCookie('cookieName')
	 * var name = cookie.getName();
	 *
	 * @param  
	 */
	public String js_getName()
	{
		if (cookie == null) return ""; //$NON-NLS-1$
		return cookie.getName();
	}

	/**
	 * Returns the cookie value.
	 *
	 * @sample
	 * var cookie = client.getCookie('cookieName')
	 * var value = cookie.getValue();
	 *
	 * @param  
	 */
	public String js_getValue()
	{
		if (cookie == null) return "";//$NON-NLS-1$
		return cookie.getValue();
	}

	/**
	 * Returns the cookie domain.
	 *
	 * @sample
	 * var cookie = client.getCookie('cookieName')
	 * var domain = cookie.getDomain();
	 *
	 * @param  
	 */
	public String js_getDomain()
	{
		if (cookie == null) return "";//$NON-NLS-1$
		return cookie.getDomain();
	}

	/**
	 * Returns the cookie path.
	 *
	 * @sample
	 * var cookie = client.getCookie('cookieName')
	 * var path = cookie.getPath();
	 *
	 * @param  
	 */
	public String js_getPath()
	{
		if (cookie == null) return "";//$NON-NLS-1$
		return cookie.getPath();
	}

	/**
	 * Returns the cookie secure attribute.
	 *
	 * @sample
	 * var cookie = client.getCookie('cookieName')
	 * var path = cookie.getSecure();
	 *
	 * @param  
	 */
	public boolean js_getSecure()
	{
		if (cookie == null) return false;
		return cookie.isSecure();
	}

	/**
	 * Returns the cookie comment.
	 *
	 * @sample
	 * var cookie = client.getCookie('cookieName')
	 * var path = cookie.getComment();
	 *
	 * @param  
	 */
	public String js_getComment()
	{
		if (cookie == null) return "";//$NON-NLS-1$
		return cookie.getComment();
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

}
