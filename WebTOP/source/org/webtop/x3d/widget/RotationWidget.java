/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.x3d.widget;

import org.webtop.x3d.SAI;
import org.webtop.x3d.NamedNode;

/**
 * <p>Title: X3DWebTOP</p>
 *
 * <p>Description: The X3D version of The Optics Project for the Web
 * (WebTOP)</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: MSU Department of Physics and Astronomy</p>
 *
 * @author Paul Cleveland, Peter Gilbert
 * @version 0.0
 */
public class RotationWidget extends ScalarWidget {
        /**
         * Event name used by the VRMLLib <code>RotationWidget</code>.
         */
        public static final String ANGLE_OUT = "angle_changed", ANGLE_IN = "set_angle",
                MIN_IN = "set_minAngle", MAX_IN = "set_maxAngle";

        //This class has no need for a typeID; it doesn't override callback().

        /**
         * Constructs a <code>RotationWidget</code> for the named widget.
         * <br>See next constructor for parameter details.
         */
        /*public RotationWidget(EAI eai,String widgetName,short widgetId,String help) {
                super(eai,widgetName,widgetId,help,ISOVER_OUT,ISACTIVE_OUT,ISOVER_IN,ISACTIVE_IN,ENABLED_IN,ANGLE_OUT,ANGLE_IN,MIN_IN,MAX_IN);
                     }*/
        /**
         * Constructs a <code>RotationWidget</code> for the given <code>NamedNode</code>.
         *
         * @param eai the <code>EAI</code> object through which to access the VRML browser.
         * @param widgetNode the <code>NamedNode</code> to associate with this <code>RotationWidget</code>.
         * @param widgetId an arbitrary integer to be used to identify this <code>RotationWidget</code>.
         * @param help an arbitrary String, as for a help message.
         */
        public RotationWidget(SAI sai, NamedNode widgetNode, short widgetId, String help) {
            super(sai, widgetNode, widgetId, help, ISOVER_OUT, ISACTIVE_OUT, ISOVER_IN, ISACTIVE_IN,
                  ENABLED_IN, ANGLE_OUT, ANGLE_IN, MIN_IN, MAX_IN);
        }

}
