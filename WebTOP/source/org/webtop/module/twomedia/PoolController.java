/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//PoolController.java
//Declares a (hopefully temporary) interface for ResolutionDialog's use.
//Davis Herring
//Created February 25 2004
//Updated February 25 2004
//Version 0.0

package org.webtop.module.twomedia;

public interface PoolController {
	public void setPoolOptions(int res, boolean normalPerVertex);
}
